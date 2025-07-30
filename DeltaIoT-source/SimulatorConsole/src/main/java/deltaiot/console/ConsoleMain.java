package deltaiot.console;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.IUsageFormatter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import deltaiot.DeltaIoTSimulator;
import deltaiot.client.ISimulationResult;
import deltaiot.client.ISimulationRunner;
import deltaiot.client.SimpleRunner;
import deltaiot.client.SimulationClient;
import deltaiot.console.Args.CommandStrategy;
import deltaiot.console.json.StrictFieldsTypeAdapterFactory;
import main.SimpleAdaptation;
import mapek.strategy.AdaptionStrategyFactory;
import mapek.strategy.IAdaptionStrategy;
import mapek.strategy.IStrategyConfiguration;
import simulator.QoS;
import simulator.QoSCalculator;
import simulator.Simulator;
import simulator.SimulatorConfig;
import simulator.SimulatorFactory;
import util.CsvFileWriter;
import util.IResultWriter;
import util.IMoteWriter;

public class ConsoleMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleMain.class);

    public ConsoleMain() {
    }

    public static void main(String[] args) {
        ConsoleMain main = new ConsoleMain();
        int returnCode = main.run(args);
        System.exit(returnCode);
    }

    private int run(String[] argv) {
        Args args = new Args();
        CommandStrategy strategy = new CommandStrategy();
        JCommander parser = JCommander.newBuilder()
            .addObject(args)
            .addCommand(CommandStrategy.ID, strategy)
            .build();

        try {
            parser.parse(argv);
            if (args.help) {
                StringBuilder sb = new StringBuilder();
                IUsageFormatter usageFormatter = parser.getUsageFormatter();
                usageFormatter.usage(sb);
                LOGGER.info("{}", sb);
                return 1;
            }

            runSimulation(args, strategy, parser);
            return 0;
        } catch (ParameterException e) {
            StringBuilder sb = new StringBuilder();
            IUsageFormatter usageFormatter = parser.getUsageFormatter();
            usageFormatter.usage(sb);
            LOGGER.error("{}\n{}", e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 2;
    }

    private void runSimulation(Args args, CommandStrategy strategy, JCommander parser) throws IOException {
        SimulatorConfig config = new SimulatorConfig(DeltaIoTSimulator.NUM_OF_RUNS);
        Simulator simulator = SimulatorFactory.createExperimentSimulator(config, new NullRunMonitor());
        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
        IResultWriter resultWriter = new CsvFileWriter(baseLocation);

        final ISimulationRunner runner;
        final String strategyName;
        String command = parser.getParsedCommand();
        if (CommandStrategy.ID.equals(command)) {
            strategyName = strategy.strategyKind.name();
            LOGGER.info("running with strategy: {}", strategy.strategyKind);
            runner = runWithAdaption(simulator, strategy, resultWriter);
        } else {
            strategyName = "none";
            LOGGER.info("running without strategy");
            runner = runNoAdaption(simulator);
        }
        ISimulationResult simulationResult = runner.run();
        List<QoS> qos = simulationResult.getQoS();
        resultWriter.saveQoS(qos, simulationResult.getStrategyId());
        QoSCalculator qoSCalculator = new QoSCalculator();
        double energyConsumptionAverage = qoSCalculator.calcEnergyConsumptionAverage(qos);
        double packetLossAverage = qoSCalculator.calcPacketLossAverage(qos);
        double score = qoSCalculator.calcScore(qos);
        LOGGER.info("result average energy {}, packet loss {}", energyConsumptionAverage, packetLossAverage);
        LOGGER.info("result score: {}", score);

        if (args.resultPath != null) {
            Result result = new Result(strategyName, energyConsumptionAverage, packetLossAverage, score);
            writeResult(result, args.resultPath);
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

    private ISimulationRunner runWithAdaption(Simulator simulator, CommandStrategy strategy, IMoteWriter moteWriter)
            throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        // Create Feedback loop
        AdaptionStrategyFactory adaptionStrategyFactory = new AdaptionStrategyFactory();
        // FeedbackLoop feedbackLoop = new QualityBasedFeedbackLoop(networkMgmt);
        IStrategyConfiguration config = readStrategyParameter(strategy.parameterFile,
                strategy.strategyKind.getStrategyConfiguration());
        IAdaptionStrategy feedbackLoop = adaptionStrategyFactory.create(strategy.strategyKind, simulationClient,
                moteWriter, config);
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
