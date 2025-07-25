package deltaiot.console;

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
        main.run(args);
    }

    private void run(String[] args) {
        // runNoAdaption();
        runWithAdaption();
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

    private void runWithAdaption() {
        ICSVWriter csvWriter = new CsvFileWriter();
        SimpleAdaptation client = new SimpleAdaptation(DeltaIoTSimulator.NUM_OF_RUNS, csvWriter);
        client.start();
    }
}
