package deltaiot.console;

import deltaiot.DeltaIoTSimulator;
import domain.Gateway;
import main.SimpleAdaptation;
import simulator.Simulator;
import util.CsvFileWriter;
import util.ICSVWriter;

public class ConsoleMain {

    public static void main(String[] args) {
        // runNoAdaption();
        runWithAdaption();
    }

    private static void runNoAdaption() {
        Simulator simul = DeltaIoTSimulator.createSimulatorForDeltaIoT(DeltaIoTSimulator.NUM_OF_RUNS);

        // Do logic
        for (int i = 0; i < 96; ++i) {
            simul.doSingleRun();
            // simul.doMultipleRuns(96);

            for (Gateway gateway : simul.getGateways()) {
                System.out.println(gateway);
            }
            /*
             * for(Mote mote: motes) { System.out.println(mote); }
             */
        }
    }

    private static void runWithAdaption() {
        ICSVWriter csvWriter = new CsvFileWriter();
        SimpleAdaptation client = new SimpleAdaptation(DeltaIoTSimulator.NUM_OF_RUNS, csvWriter);
        client.start();
    }
}
