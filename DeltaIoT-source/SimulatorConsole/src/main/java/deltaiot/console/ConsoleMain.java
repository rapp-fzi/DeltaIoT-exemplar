package deltaiot.console;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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
import deltaiot.client.SimulationClient;
import main.SimpleAdaptation;
import simulator.QoS;
import simulator.Simulator;
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

            Simulator simulator = DeltaIoTSimulator.createSimulatorForDeltaIoT(DeltaIoTSimulator.NUM_OF_RUNS);
            if (cmdLine.hasOption('a')) {
                runWithAdaption(simulator);
            } else {
                runNoAdaption(simulator);
            }

            return 0;
        } catch (ParseException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("ConsoleMain", options);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 1;
    }

    private void runNoAdaption(Simulator simulator) throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        // Do logic
        for (int i = 0; i < simulator.getNumOfRuns(); ++i) {
            simulator.doSingleRun();
        }
        ArrayList<QoS> result = simulationClient.getNetworkQoS(DeltaIoTSimulator.NUM_OF_RUNS);
        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
        IQOSWriter qosWriter = new CsvFileWriter(baseLocation);
        qosWriter.saveQoS(result, "NonAdaptiveDeltaIoTStrategy");
    }

    private void runWithAdaption(Simulator simulator) throws IOException {
        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
        ICSVWriter csvWriter = new CsvFileWriter(baseLocation);
        SimulationClient simulationClient = new SimulationClient(simulator);
        SimpleAdaptation adaption = new SimpleAdaptation(simulationClient, csvWriter);
        adaption.start();
    }
}
