package deltaiot.gui.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.DeltaIoTSimulator;
import deltaiot.client.ISimulationResult;
import deltaiot.client.ISimulationRunner;
import deltaiot.client.SimpleRunner;
import deltaiot.client.SimulationClient;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import simulator.IRunMonitor;
import simulator.QoS;
import simulator.QoSCalculator;
import simulator.Simulator;
import simulator.SimulatorConfig;
import simulator.SimulatorFactory;
import util.CsvFileWriter;
import util.ICSVWriter;
import util.IQOSWriter;

public class ServiceEmulation extends Service<Void> implements ISimulatorProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEmulation.class);

    private final IDataDisplay dataDisplay;
    private final IRunMonitor runMonitor;
    private final Button btnDisplay;

    private Simulator simul;

    public ServiceEmulation(IDataDisplay dataDisplay, IRunMonitor runMonitor, Button btnDisplay) {
        this.dataDisplay = dataDisplay;
        this.runMonitor = runMonitor;
        this.btnDisplay = btnDisplay;
    }

    @Override
    public Simulator getSimulator() {
        return simul;
    }

    @Override
    protected void succeeded() {
        dataDisplay.displayData(simul, "Without Adaptation", 0);
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                btnDisplay.setDisable(true);
                try {
                    SimulatorConfig config = createConfig();
                    simul = SimulatorFactory.createExperimentSimulator(config, runMonitor);
                    Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
                    ICSVWriter csvWriter = new CsvFileWriter(baseLocation);
                    ISimulationRunner runner = runNoAdaption(simul);
                    executeRunner(runner, csvWriter);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }

                btnDisplay.setDisable(false);
                return null;
            }
        };
    }

    private SimulatorConfig createConfig() {
        SimulatorConfig config = new SimulatorConfig(DeltaIoTSimulator.NUM_OF_RUNS);
        return config;
    }

    private ISimulationRunner runNoAdaption(Simulator simulator) throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        SimpleRunner simpleRunner = new SimpleRunner(simulationClient);
        return simpleRunner;
    }

    private void executeRunner(ISimulationRunner runner, IQOSWriter qosWriter) throws IOException {
        ISimulationResult result = runner.run();
        List<QoS> qos = result.getQoS();
        qosWriter.saveQoS(qos, result.getStrategyId());

        QoSCalculator qoSCalculator = new QoSCalculator();
        double energyConsumptionAverage = qoSCalculator.calcEnergyConsumptionAverage(qos);
        double packetLossAverage = qoSCalculator.calcPacketLossAverage(qos);
        double score = qoSCalculator.calcScore(qos);
        LOGGER.info("result average energy {}, packet loss {}", energyConsumptionAverage, packetLossAverage);
        LOGGER.info("result score: {}", score);
    }
}
