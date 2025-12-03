package deltaiot.console;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

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
import picocli.CommandLine.Option;
import simulator.QoS;
import simulator.QoSCalculator;
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
public class ConsoleMain implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleMain.class);

    @Option(names = { "-r", "--result" }, description = "result file")
    private Path resultPath;

    @Option(names = { "-n", "--num_runs" }, description = "number of runs")
    private int num_runs = DeltaIoTSimulator.NUM_OF_RUNS;

    @Option(names = { "-s", "--seed" }, description = "PRNG seed")
    public Long seed;

    public ConsoleMain() {
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ConsoleMain()).execute(args);
        System.exit(exitCode);
    }

    @Command(name = "strategy", description = "Simulate with strategy", mixinStandardHelpOptions = true)
    int strategy(@Option(names = { "-a",
            "--adaption" }, required = true, description = "Adatption type: ${COMPLETION-CANDIDATES}") Kind strategyKind,
            @Option(names = { "-p",
                    "--param" }, required = true, description = "json parameter file") Path parameterFile)
            throws IOException {

        Random randomGenerator = new Random();
        if (seed != null) {
            randomGenerator.setSeed(seed);
        }
        SimulatorConfig config = new SimulatorConfig(num_runs, randomGenerator);
        Simulator simulator = SimulatorFactory.createExperimentSimulator(config, new NullRunMonitor());
        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
        IResultWriter resultWriter = new CsvFileWriter(baseLocation);

        final ISimulationRunner runner;
        final String strategyName;
        final IStrategyConfiguration strategyConfig;

        strategyName = strategyKind.name();
        LOGGER.info("running with strategy: {}", strategyKind);
        strategyConfig = readStrategyParameter(parameterFile, strategyKind.getStrategyConfiguration());
        runner = runWithAdaption(simulator, strategyKind, strategyConfig, resultWriter);

        ISimulationResult simulationResult = runner.run();
        List<QoS> qos = simulationResult.getQoS();
        QoSCalculator qoSCalculator = new QoSCalculator();
        double energyConsumptionAverage = qoSCalculator.calcEnergyConsumptionAverage(qos);
        double packetLossAverage = qoSCalculator.calcPacketLossAverage(qos);
        double score = qoSCalculator.calcScore(qos);
        LOGGER.info("result average energy {}, packet loss {}", energyConsumptionAverage, packetLossAverage);
        LOGGER.info("result score: {}", score);

        QoSResult qosResult = new QoSResult(simulationResult.getStrategyId(), qos, energyConsumptionAverage,
                packetLossAverage, score);
        IQOSWriter qosWriter = new JsonQOSWriter(baseLocation);
        qosWriter.saveQoS(qosResult);

        if (resultPath != null) {
            Result result = new Result(strategyName, strategyConfig, num_runs, energyConsumptionAverage,
                    packetLossAverage, score, qos);
            writeResult(result, resultPath);
        }

        return 0;
    }

    @Override
    public Integer call() throws Exception {
        try {
            runSimulation();
            return 0;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 2;
    }

    private void runSimulation() throws IOException {
        Random randomGenerator = new Random();
        if (seed != null) {
            randomGenerator.setSeed(seed);
        }
        SimulatorConfig config = new SimulatorConfig(num_runs, randomGenerator);
        Simulator simulator = SimulatorFactory.createExperimentSimulator(config, new NullRunMonitor());
        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");

        final ISimulationRunner runner;
        final String strategyName;
        final IStrategyConfiguration strategyConfig;

        strategyName = "none";
        strategyConfig = null;
        LOGGER.info("running without strategy");
        runner = runNoAdaption(simulator);

        ISimulationResult simulationResult = runner.run();
        List<QoS> qos = simulationResult.getQoS();
        QoSCalculator qoSCalculator = new QoSCalculator();
        double energyConsumptionAverage = qoSCalculator.calcEnergyConsumptionAverage(qos);
        double packetLossAverage = qoSCalculator.calcPacketLossAverage(qos);
        double score = qoSCalculator.calcScore(qos);
        LOGGER.info("result average energy {}, packet loss {}", energyConsumptionAverage, packetLossAverage);
        LOGGER.info("result score: {}", score);

        QoSResult qosResult = new QoSResult(simulationResult.getStrategyId(), qos, energyConsumptionAverage,
                packetLossAverage, score);
        IQOSWriter qosWriter = new JsonQOSWriter(baseLocation);
        qosWriter.saveQoS(qosResult);

        if (resultPath != null) {
            Result result = new Result(strategyName, strategyConfig, num_runs, energyConsumptionAverage,
                    packetLossAverage, score, qos);
            writeResult(result, resultPath);
        }
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
