package main;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.client.ISimulationResult;
import deltaiot.client.ISimulationRunner;
import deltaiot.client.SimulationClient;
import deltaiot.client.SimulationResult;
import mapek.FeedbackLoop;
import mapek.IAdaptionStrategy;
import simulator.QoS;
import simulator.Simulator;
import util.IMoteWriter;

public class SimpleAdaptation implements ISimulationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAdaptation.class);

    private final IMoteWriter moteWriter;
    private final SimulationClient networkMgmt;

    public SimpleAdaptation(SimulationClient networkMgmt, IMoteWriter moteWriter) {
        this.moteWriter = moteWriter;
        // Create a simulation client object
        this.networkMgmt = networkMgmt;
    }

    @Override
    public ISimulationResult run() throws IOException {
        // Create Feedback loop
        // FeedbackLoop feedbackLoop = new FeedbackLoop(numOfRuns, probe, effector, csvWriter);
        IAdaptionStrategy feedbackLoop = new FeedbackLoop(networkMgmt, moteWriter);
        // FeedbackLoop feedbackLoop = new QualityBasedFeedbackLoop(networkMgmt);

        // StartFeedback loop
        feedbackLoop.start();

        int numOfRuns = networkMgmt.getSimulator()
            .getNumOfRuns();
        List<QoS> result = networkMgmt.getNetworkQoS(numOfRuns);

        LOGGER.info("Run, PacketLoss, EnergyConsumption");
        result.forEach(qos -> LOGGER.info("{}", qos));

        return new SimulationResult(feedbackLoop.getId(), result);

    }

    public Simulator getSimulator() {
        return networkMgmt.getSimulator();
    }
}
