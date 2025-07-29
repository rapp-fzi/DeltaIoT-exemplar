package deltaiot.gui.service;

import java.io.IOException;

import deltaiot.client.ISimulationRunner;
import deltaiot.client.SimulationClient;
import javafx.scene.control.Button;
import main.SimpleAdaptation;
import mapek.strategy.AdaptionStrategyFactory;
import mapek.strategy.AdaptionStrategyFactory.Kind;
import mapek.strategy.IAdaptionStrategy;
import mapek.strategy.IStrategyConfiguration;
import mapek.strategy.StrategyConfigurationDefault;
import simulator.IRunMonitor;
import simulator.Simulator;
import util.IMoteWriter;

public class ServiceAdaption extends BaseServiceSimulation {
    public ServiceAdaption(IDataDisplay dataDisplay, IRunMonitor runMonitor, Button btnDisplay) {
        super(dataDisplay, runMonitor, btnDisplay);
    }

    @Override
    protected String getName() {
        return "With Adaptation";
    }

    @Override
    protected ISimulationRunner createRunner(Simulator simulator, IMoteWriter moteWriter) throws IOException {
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

}
