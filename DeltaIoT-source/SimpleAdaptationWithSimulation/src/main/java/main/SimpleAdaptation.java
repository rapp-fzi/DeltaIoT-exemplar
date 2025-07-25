package main;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.client.Effector;
import deltaiot.client.ISimulationRunner;
import deltaiot.client.Probe;
import deltaiot.client.SimulationClient;
import mapek.FeedbackLoop;
import simulator.QoS;
import simulator.Simulator;
import util.ICSVWriter;

public class SimpleAdaptation implements ISimulationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAdaptation.class);

    private final ICSVWriter csvWriter;
    private final SimulationClient networkMgmt;

    public SimpleAdaptation(SimulationClient networkMgmt, ICSVWriter csvWriter) {
        this.csvWriter = csvWriter;
        // Create a simulation client object
        this.networkMgmt = networkMgmt;
    }

    @Override
    public void run() throws IOException {
        // get probe and effectors
        Probe probe = networkMgmt.getProbe();
        Effector effector = networkMgmt.getEffector();

        // Create Feedback loop
        int numOfRuns = networkMgmt.getSimulator()
            .getNumOfRuns();
        FeedbackLoop feedbackLoop = new FeedbackLoop(numOfRuns, probe, effector, csvWriter);
        // FeedbackLoop feedbackLoop = new QualityBasedFeedbackLoop(networkMgmt);

        // StartFeedback loop
        feedbackLoop.start();

        ArrayList<QoS> result = networkMgmt.getNetworkQoS(numOfRuns);

        LOGGER.info("Run, PacketLoss, EnergyConsumption");
        result.forEach(qos -> LOGGER.info("{}", qos));

        csvWriter.saveQoS(result, feedbackLoop.getId());

    }

    public Simulator getSimulator() {
        return networkMgmt.getSimulator();
    }
}
