package deltaiot.client;

import java.io.IOException;
import java.util.List;

import simulator.QoS;
import simulator.Simulator;
import util.IQOSWriter;

public class SimpleRunner implements ISimulationRunner {
    private final SimulationClient simulationClient;
    private final IQOSWriter qosWriter;

    public SimpleRunner(SimulationClient simulationClient, IQOSWriter qosWriter) {
        this.simulationClient = simulationClient;
        this.qosWriter = qosWriter;
    }

    @Override
    public SimulationResult run() throws IOException {
        Simulator simulator = simulationClient.getSimulator();
        for (int i = 0; i < simulator.getNumOfRuns(); ++i) {
            simulator.doSingleRun();
        }

        List<QoS> result = simulationClient.getNetworkQoS(simulator.getNumOfRuns());
        qosWriter.saveQoS(result, "NonAdaptiveDeltaIoTStrategy");
        return new SimulationResult(result);
    }

}
