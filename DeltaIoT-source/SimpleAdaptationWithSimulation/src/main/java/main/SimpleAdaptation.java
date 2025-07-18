package main;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.DeltaIoTSimulator;
import deltaiot.client.Effector;
import deltaiot.client.Probe;
import deltaiot.client.SimulationClient;
import mapek.EAFeedbackLoopStrategy1a;
import mapek.FeedbackLoop;
import simulator.QoS;
import simulator.Simulator;
import util.CsvFileWriter;
import util.ICSVWriter;

public class SimpleAdaptation {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAdaptation.class);

    private final int numOfRuns;
    private final ICSVWriter csvWriter;
    private final SimulationClient networkMgmt;

    public SimpleAdaptation(int numOfRuns, ICSVWriter csvWriter) {
        this.numOfRuns = numOfRuns;
        this.csvWriter = csvWriter;
        // Create a simulation client object
        this.networkMgmt = new SimulationClient(numOfRuns);
    }

    public void start() {
        // get probe and effectors
        Probe probe = networkMgmt.getProbe();
        Effector effector = networkMgmt.getEffector();

        // Create Feedback loop
        // FeedbackLoop feedbackLoop = new FeedbackLoop(numOfRuns, probe, effector, csvWriter);
        FeedbackLoop feedbackLoop = new EAFeedbackLoopStrategy1a(numOfRuns, probe, effector, csvWriter);
        // FeedbackLoop feedbackLoop = new QualityBasedFeedbackLoop(networkMgmt);

        // StartFeedback loop
        feedbackLoop.start();

        ArrayList<QoS> result = networkMgmt.getNetworkQoS(numOfRuns);

        LOGGER.info("Run, PacketLoss, EnergyConsumption");
        result.forEach(qos -> LOGGER.info("{}", qos));

        csvWriter.saveQoS(result, feedbackLoop.getId());

    }

    public static void main(String[] args) {
        ICSVWriter csvWriter = new CsvFileWriter();
        SimpleAdaptation client = new SimpleAdaptation(DeltaIoTSimulator.NUM_OF_RUNS, csvWriter);
        client.start();
    }

    public Simulator getSimulator() {
        return networkMgmt.getSimulator();
    }
}
