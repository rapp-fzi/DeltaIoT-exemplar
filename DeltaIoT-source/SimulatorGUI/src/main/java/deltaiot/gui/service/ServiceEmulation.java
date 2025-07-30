package deltaiot.gui.service;

import java.io.IOException;
import java.nio.file.Path;

import deltaiot.client.ISimulationRunner;
import deltaiot.client.SimpleRunner;
import deltaiot.client.SimulationClient;
import javafx.scene.control.Button;
import simulator.IRunMonitor;
import simulator.Simulator;
import util.IMoteWriter;

public class ServiceEmulation extends BaseServiceSimulation {
    public ServiceEmulation(IDataDisplay dataDisplay, IRunMonitor runMonitor, Path resultLocation, Button btnDisplay) {
        super(dataDisplay, runMonitor, resultLocation, btnDisplay);
    }

    @Override
    protected String getName() {
        return "NonAdaptiveDeltaIoTStrategy";
    }

    @Override
    protected ISimulationRunner createRunner(Simulator simulator, IMoteWriter moteWriter) throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        SimpleRunner simpleRunner = new SimpleRunner(simulationClient);
        return simpleRunner;
    }
}
