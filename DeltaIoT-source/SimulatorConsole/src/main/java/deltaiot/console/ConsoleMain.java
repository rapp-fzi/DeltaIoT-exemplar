package deltaiot.console;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import deltaiot.DeltaIoTSimulator;
import deltaiot.client.ISimulationResult;
import deltaiot.client.ISimulationRunner;
import deltaiot.client.SimpleRunner;
import deltaiot.client.SimulationClient;
import deltaiot.console.json.StrictFieldsTypeAdapterFactory;
import main.SimpleAdaptation;
import mapek.strategy.AdaptionStrategyFactory;
import mapek.strategy.AdaptionStrategyFactory.Kind;
import mapek.strategy.IAdaptionStrategy;
import mapek.strategy.IStrategyConfiguration;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import simulator.QoS;
import simulator.QoSCalculator;
import simulator.QoSValidator;
import simulator.Simulator;
import simulator.SimulatorConfig;
import simulator.SimulatorFactory;
import util.CsvFileWriter;
import util.IMoteWriter;
import util.IQOSWriter;
import util.IResultWriter;
import util.JsonQOSWriter;
import util.QoSResult;

@Command(name = "SimulatorConsole", mixinStandardHelpOptions = true)
public class ConsoleMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleMain.class);

    @Option(names = { "-r", "--result" }, description = "result file")
    private Path resultPath;

    @Option(names = { "-n", "--num_runs" }, description = "number of runs")
    private int num_runs = DeltaIoTSimulator.NUM_OF_RUNS;

    @Option(names = { "-s", "--seed" }, description = "PRNG seed")
    public Long seed;

    @Option(names = { "--no_validation" }, description = "disable QoS range validation")
    public boolean no_validation = false;

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new ConsoleMain());
        IExecutionExceptionHandler exceptionHandler = new IExecutionExceptionHandler() {

            @Override
            public int handleExecutionException(Exception e, CommandLine commandLine, ParseResult fullParseResult)
                    throws Exception {
                LOGGER.error(e.getMessage(), e);
                return 2;
            }
        };
        commandLine.setExecutionExceptionHandler(exceptionHandler);
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Command(name = "noadaption", description = "Simulate without adaption", mixinStandardHelpOptions = true)
    private void no_adaption() throws IOException {
        Simulator simulator = createSimulator();
        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");

        LOGGER.info("running without strategy");
        ISimulationRunner runner = runNoAdaption(simulator);

        ISimulationResult simulationResult = runner.run();
        processSimulationResult(simulationResult, "none", null, baseLocation);
    }

    @Command(name = "strategy", description = "Simulate with adaption strategy", mixinStandardHelpOptions = true)
    void strategy(@Option(names = { "-a",
            "--adaption" }, required = true, description = "Adatption type: ${COMPLETION-CANDIDATES}") Kind strategyKind,
            @Option(names = { "-p",
                    "--param" }, required = true, description = "json parameter file") Path parameterFile)
            throws IOException {
        Simulator simulator = createSimulator();
        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
        IResultWriter resultWriter = new CsvFileWriter(baseLocation);

        LOGGER.info("running with strategy: {}", strategyKind);
        IStrategyConfiguration strategyConfig = readStrategyParameter(parameterFile,
                strategyKind.getStrategyConfiguration());
        ISimulationRunner runner = runWithAdaption(simulator, strategyKind, strategyConfig, resultWriter);

        ISimulationResult simulationResult = runner.run();
        processSimulationResult(simulationResult, strategyKind.name(), strategyConfig, baseLocation);
    }

    private void processSimulationResult(ISimulationResult simulationResult, String strategyName,
            IStrategyConfiguration strategyConfig, Path baseLocation) throws IOException {
        List<QoS> qos = simulationResult.getQoS();
        if (no_validation) {
            QoSValidator validator = new QoSValidator();
            validator.validate(qos);
        }
        QoSCalculator qoSCalculator = new QoSCalculator();
        DoubleSummaryStatistics energyStats = qoSCalculator.calcEnergyConsumptionStatistics(qos);
        DoubleSummaryStatistics packetStats = qoSCalculator.calcPacketLossStatistics(qos);
        double energyConsumptionAverage = qoSCalculator.calcEnergyConsumptionAverage(qos);
        double packetLossAverage = qoSCalculator.calcPacketLossAverage(qos);
        double averageScore = qoSCalculator.averageScore(qos);
        LOGGER.info("result min/max energy:      {} / {}", energyStats.getMin(), energyStats.getMax());
        LOGGER.info("result min/max packet loss: {} / {}", packetStats.getMin(), packetStats.getMax());
        LOGGER.info("result average energy:      {}", energyConsumptionAverage);
        LOGGER.info("result average packet loss: {}", packetLossAverage);
        LOGGER.info("result average score:       {}", averageScore);

        QoSResult qosResult = new QoSResult(simulationResult.getStrategyId(), qos, energyConsumptionAverage,
                packetLossAverage, averageScore);
        IQOSWriter qosWriter = new JsonQOSWriter(baseLocation);
        qosWriter.saveQoS(qosResult);

        if (resultPath != null) {
            Result result = new Result(strategyName, strategyConfig, num_runs, energyStats.getMin(),
                    energyStats.getMax(), energyConsumptionAverage, packetStats.getMin(), packetStats.getMax(),
                    packetLossAverage, averageScore, qos);
            writeResult(result, resultPath);
        }
    }

    private Simulator createSimulator() {
        SimulatorConfig config = createSimulatorConfig();
        Simulator simulator = SimulatorFactory.createExperimentSimulator(config, new NullRunMonitor());
        return simulator;
    }

    private SimulatorConfig createSimulatorConfig() {
        Random randomGenerator = crateRandomGenerator();
        SimulatorConfig config = new SimulatorConfig(num_runs, randomGenerator);
        return config;
    }

    private Random crateRandomGenerator() {
        Random randomGenerator = new Random();
        if (seed != null) {
            randomGenerator.setSeed(seed);
        }
        return randomGenerator;
    }

    private void writeResult(Result result, Path resultFile) throws IOException {
        LOGGER.info("write result to: {}", resultFile);
        Gson gson = new GsonBuilder().serializeNulls()
            .setPrettyPrinting()
            .create();
        try (Writer writer = Files.newBufferedWriter(resultFile, StandardCharsets.UTF_8)) {
            gson.toJson(result, writer);
        }
    }

    private ISimulationRunner runNoAdaption(Simulator simulator) throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        SimpleRunner simpleRunner = new SimpleRunner(simulationClient);
        return simpleRunner;
    }

    private ISimulationRunner runWithAdaption(Simulator simulator, Kind strategyKind,
            IStrategyConfiguration strategyConfig, IMoteWriter moteWriter) throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        // Create Feedback loop
        AdaptionStrategyFactory adaptionStrategyFactory = new AdaptionStrategyFactory();
        // FeedbackLoop feedbackLoop = new QualityBasedFeedbackLoop(networkMgmt);
        IAdaptionStrategy feedbackLoop = adaptionStrategyFactory.create(strategyKind, simulationClient, moteWriter,
                strategyConfig);
        SimpleAdaptation adaption = new SimpleAdaptation(simulationClient, feedbackLoop);
        return adaption;
    }

    private <T extends IStrategyConfiguration> T readStrategyParameter(Path parameterFile, Class<T> paramClass)
            throws IOException {
        LOGGER.info("read strategy parameters from: {}", parameterFile);
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new StrictFieldsTypeAdapterFactory())
            .create();
        try (Reader reader = Files.newBufferedReader(parameterFile, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, paramClass);
        }
    }
}
