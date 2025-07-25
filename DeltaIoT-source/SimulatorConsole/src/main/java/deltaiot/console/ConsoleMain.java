package deltaiot.console;

import java.io.IOException;

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
import domain.Gateway;
import main.SimpleAdaptation;
import simulator.Simulator;
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

            if (cmdLine.hasOption('a')) {
                runWithAdaption();
            } else {
                runNoAdaption();
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

    private void runNoAdaption() {
        Simulator simul = DeltaIoTSimulator.createSimulatorForDeltaIoT(DeltaIoTSimulator.NUM_OF_RUNS);

        // Do logic
        for (int i = 0; i < 96; ++i) {
            simul.doSingleRun();
            // simul.doMultipleRuns(96);

            for (Gateway gateway : simul.getGateways()) {
                LOGGER.info("{}", gateway);
            }
            /*
             * for(Mote mote: motes) { System.out.println(mote); }
             */
        }
    }

    private void runWithAdaption() throws IOException {
        ICSVWriter csvWriter = new CsvFileWriter();
        SimpleAdaptation client = new SimpleAdaptation(DeltaIoTSimulator.NUM_OF_RUNS, csvWriter);
        client.start();
    }
}
