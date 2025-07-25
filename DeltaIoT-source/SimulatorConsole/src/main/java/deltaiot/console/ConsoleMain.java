package deltaiot.console;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.DeltaIoTSimulator;
import deltaiot.client.ISimulationResult;
import deltaiot.client.ISimulationRunner;
import deltaiot.client.SimpleRunner;
import deltaiot.client.SimulationClient;
import main.SimpleAdaptation;
import simulator.QoS;
import simulator.QoSCalculator;
import simulator.Simulator;
import simulator.SimulatorConfig;
import simulator.SimulatorFactory;
import util.CsvFileWriter;
import util.ICSVWriter;

public class ConsoleMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleMain.class);

    public ConsoleMain() {
    }

    public static void main(String[] args) {
        ConsoleMain main = new ConsoleMain();
        int returnCode = main.run(args);
        System.exit(returnCode);
    }

    private int run(String[] args) {
        Options options = new Options().addOption(Option.builder("a")
            .longOpt("adaption")
            .desc("Run with adaption")
            .build());
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmdLine = parser.parse(options, args);
            runSimulation(cmdLine);
            return 0;
        } catch (ParseException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("ConsoleMain", options);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 1;
    }

    private void runSimulation(CommandLine cmdLine) throws IOException {
        SimulatorConfig config = new SimulatorConfig(DeltaIoTSimulator.NUM_OF_RUNS);
        Simulator simulator = SimulatorFactory.createExperimentSimulator(config);
        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
        ICSVWriter csvWriter = new CsvFileWriter(baseLocation);

        final ISimulationRunner runner;
        if (cmdLine.hasOption('a')) {
            runner = runWithAdaption(simulator, csvWriter);
        } else {
            runner = runNoAdaption(simulator);
        }
        ISimulationResult result = runner.run();
        List<QoS> qos = result.getQoS();
        csvWriter.saveQoS(qos, result.getStrategyId());
        QoSCalculator qoSCalculator = new QoSCalculator();
        double energyConsumptionAverage = qoSCalculator.calcEnergyConsumptionAverage(qos);
        double packetLossAverage = qoSCalculator.calcPacketLossAverage(qos);
        double score = qoSCalculator.calcScore(qos);
        LOGGER.info("result average energy {}, packet loss {}", energyConsumptionAverage, packetLossAverage);
        LOGGER.info("result score: {}", score);
    }

    private ISimulationRunner runNoAdaption(Simulator simulator) throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        SimpleRunner simpleRunner = new SimpleRunner(simulationClient);
        return simpleRunner;
    }

    private ISimulationRunner runWithAdaption(Simulator simulator, ICSVWriter csvWriter) throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        SimpleAdaptation adaption = new SimpleAdaptation(simulationClient, csvWriter);
        return adaption;
    }
}
