package deltaiot.client;

import java.io.IOException;
import java.util.List;

import simulator.QoS;
import simulator.Simulator;

public class SimpleRunner implements ISimulationRunner {
    private final SimulationClient simulationClient;

    public SimpleRunner(SimulationClient simulationClient) {
        this.simulationClient = simulationClient;
    }

    @Override
    public ISimulationResult run() throws IOException {
        Simulator simulator = simulationClient.getSimulator();
        for (int i = 0; i < simulator.getNumOfRuns(); ++i) {
            simulator.doSingleRun();
        }

        List<QoS> result = simulationClient.getNetworkQoS(simulator.getNumOfRuns());
        return new SimulationResult("NonAdaptiveDeltaIoTStrategy", result);
    }

}
