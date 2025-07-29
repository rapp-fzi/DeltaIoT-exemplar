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
import deltaiot.client.SimulationClient;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import main.SimpleAdaptation;
import mapek.strategy.AdaptionStrategyFactory;
import mapek.strategy.AdaptionStrategyFactory.Kind;
import mapek.strategy.IAdaptionStrategy;
import mapek.strategy.IStrategyConfiguration;
import mapek.strategy.StrategyConfigurationDefault;
import simulator.QoS;
import simulator.QoSCalculator;
import simulator.Simulator;
import simulator.SimulatorConfig;
import simulator.SimulatorFactory;
import util.CsvFileWriter;
import util.ICSVWriter;
import util.IMoteWriter;
import util.IQOSWriter;

public class ServiceAdaption extends Service<Void> implements ISimulatorProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAdaption.class);

    private final IDataDisplay dataDisplay;
    private final Button btnDisplay;

    private Simulator simul;

    public ServiceAdaption(IDataDisplay dataDisplay, Button btnDisplay) {
        this.dataDisplay = dataDisplay;
        this.btnDisplay = btnDisplay;
    }

    @Override
    public Simulator getSimulator() {
        return simul;
    }

    @Override
    protected void succeeded() {
        dataDisplay.displayData(simul, "With Adaptation", 1);
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                btnDisplay.setDisable(true);
                try {
                    SimulatorConfig config = createConfig();
                    simul = SimulatorFactory.createExperimentSimulator(config);
                    Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
                    ICSVWriter csvWriter = new CsvFileWriter(baseLocation);
                    ISimulationRunner runner = runWithAdaption(simul, csvWriter);
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

    private ISimulationRunner runWithAdaption(Simulator simulator, IMoteWriter moteWriter) throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        // Create Feedback loop
        AdaptionStrategyFactory adaptionStrategyFactory = new AdaptionStrategyFactory();
        // FeedbackLoop feedbackLoop = new QualityBasedFeedbackLoop(networkMgmt);
        Kind kind = Kind.Default;
        IStrategyConfiguration config = new StrategyConfigurationDefault();
        IAdaptionStrategy feedbackLoop = adaptionStrategyFactory.create(kind, simulationClient, moteWriter, config);
        SimpleAdaptation adaption = new SimpleAdaptation(simulationClient, feedbackLoop);
        return adaption;
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
