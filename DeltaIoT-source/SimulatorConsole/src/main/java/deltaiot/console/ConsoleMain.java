package deltaiot.console;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import deltaiot.client.ISimulationRunner;
import deltaiot.client.SimpleRunner;
import deltaiot.client.SimulationClient;
import main.SimpleAdaptation;
import simulator.Simulator;
import simulator.SimulatorConfig;
import simulator.SimulatorFactory;
import util.CsvFileWriter;
import util.ICSVWriter;
import util.IQOSWriter;

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
            SimulatorConfig config = new SimulatorConfig(DeltaIoTSimulator.NUM_OF_RUNS);
            Simulator simulator = SimulatorFactory.createExperimentSimulator(config);
            final ISimulationRunner runner;
            if (cmdLine.hasOption('a')) {
                runner = runWithAdaption(simulator);
            } else {
                runner = runNoAdaption(simulator);
            }
            runner.run();

            return 0;
        } catch (ParseException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("ConsoleMain", options);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 1;
    }

    private ISimulationRunner runNoAdaption(Simulator simulator) throws IOException {
        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
        IQOSWriter qosWriter = new CsvFileWriter(baseLocation);
        SimulationClient simulationClient = new SimulationClient(simulator);
        SimpleRunner simpleRunner = new SimpleRunner(simulationClient, qosWriter);
        return simpleRunner;
    }

    private ISimulationRunner runWithAdaption(Simulator simulator) throws IOException {
        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
        ICSVWriter csvWriter = new CsvFileWriter(baseLocation);
        SimulationClient simulationClient = new SimulationClient(simulator);
        SimpleAdaptation adaption = new SimpleAdaptation(simulationClient, csvWriter);
        return adaption;
    }
}
