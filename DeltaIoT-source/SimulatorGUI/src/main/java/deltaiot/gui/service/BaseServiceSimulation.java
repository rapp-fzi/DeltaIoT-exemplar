package deltaiot.gui.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.DeltaIoTSimulator;
import deltaiot.client.ISimulationResult;
import deltaiot.client.ISimulationRunner;
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
import util.IMoteWriter;
import util.IQOSWriter;
import util.IResultWriter;
import util.JsonQOSWriter;
import util.QoSResult;

public abstract class BaseServiceSimulation extends Service<Void> implements ISimulatorProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceSimulation.class);

    private final IDataDisplay dataDisplay;
    private final IRunMonitor runMonitor;
    private final Path resultLocation;
    private final Button btnDisplay;

    private Simulator simul;

    public BaseServiceSimulation(IDataDisplay dataDisplay, IRunMonitor runMonitor, Path resultLocation,
            Button btnDisplay) {
        this.dataDisplay = dataDisplay;
        this.runMonitor = runMonitor;
        this.resultLocation = resultLocation;
        this.btnDisplay = btnDisplay;
    }

    @Override
    public Simulator getSimulator() {
        return simul;
    }

    @Override
    protected void succeeded() {
        List<QoS> qosList = simul.getQosValues();
        dataDisplay.displayData(qosList, getName());
    }

    protected abstract String getName();

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                btnDisplay.setDisable(true);
                try {
                    SimulatorConfig config = createConfig();
                    simul = SimulatorFactory.createExperimentSimulator(config, runMonitor);
                    IResultWriter resultWriter = new CsvFileWriter(resultLocation);
                    ISimulationRunner runner = createRunner(simul, resultWriter);
                    IQOSWriter qosWriter = new JsonQOSWriter(resultLocation);
                    executeRunner(runner, qosWriter);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }

                btnDisplay.setDisable(false);
                return null;
            }
        };
    }

    protected abstract ISimulationRunner createRunner(Simulator simulator, IMoteWriter moteWriter) throws IOException;

    protected SimulatorConfig createConfig() {
        SimulatorConfig config = new SimulatorConfig(DeltaIoTSimulator.NUM_OF_RUNS);
        return config;
    }

    protected void executeRunner(ISimulationRunner runner, IQOSWriter qosWriter) throws IOException {
        ISimulationResult result = runner.run();
        List<QoS> qos = result.getQoS();
        QoSCalculator qoSCalculator = new QoSCalculator();
        double energyConsumptionAverage = qoSCalculator.calcEnergyConsumptionAverage(qos);
        double packetLossAverage = qoSCalculator.calcPacketLossAverage(qos);
        double score = qoSCalculator.calcScore(qos);
        LOGGER.info("result average energy {}, packet loss {}", energyConsumptionAverage, packetLossAverage);
        LOGGER.info("result score: {}", score);

        QoSResult qosResult = new QoSResult(result.getStrategyId(), qos, energyConsumptionAverage, packetLossAverage,
                score);
        qosWriter.saveQoS(qosResult);
    }
}
