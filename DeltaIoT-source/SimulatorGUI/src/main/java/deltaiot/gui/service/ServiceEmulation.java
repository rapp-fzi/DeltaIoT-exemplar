package deltaiot.gui.service;

import java.io.IOException;

import deltaiot.client.ISimulationRunner;
import deltaiot.client.SimpleRunner;
import deltaiot.client.SimulationClient;
import javafx.scene.control.Button;
import simulator.IRunMonitor;
import simulator.Simulator;
import util.IMoteWriter;

public class ServiceEmulation extends BaseServiceSimulation {
    public ServiceEmulation(IDataDisplay dataDisplay, IRunMonitor runMonitor, Button btnDisplay) {
        super(dataDisplay, runMonitor, btnDisplay);
    }

    @Override
    protected String getName() {
        return "Without Adaptation";
    }

    @Override
    protected ISimulationRunner createRunner(Simulator simulator, IMoteWriter moteWriter) throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        SimpleRunner simpleRunner = new SimpleRunner(simulationClient);
        return simpleRunner;
    }
}
