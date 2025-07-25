package deltaiot.console;

import deltaiot.DeltaIoTSimulator;
import domain.Gateway;
import simulator.Simulator;

public class Main {

    public static void main(String[] args) {
        run();
    }

    private static void run() {
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
}
