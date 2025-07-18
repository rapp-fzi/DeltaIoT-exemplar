package main;

import java.util.ArrayList;

import deltaiot.DeltaIoTSimulator;
import deltaiot.client.Effector;
import deltaiot.client.Probe;
import deltaiot.client.SimulationClient;
import mapek.FeedbackLoop;
import simulator.QoS;
import simulator.Simulator;
import util.CsvFileWriter;

public class SimpleAdaptation {
    private final int numOfRuns;

    SimulationClient networkMgmt;

    public SimpleAdaptation(int numOfRuns) {
        this.numOfRuns = numOfRuns;
    }

    public void start() {

        // Create a simulation client object
        networkMgmt = new SimulationClient(numOfRuns);

        // Create Feedback loop
        FeedbackLoop feedbackLoop = new FeedbackLoop(numOfRuns);
        // FeedbackLoop feedbackLoop = new QualityBasedFeedbackLoop(networkMgmt);

        // get probe and effectors
        Probe probe = networkMgmt.getProbe();
        Effector effector = networkMgmt.getEffector();

        // Connect probe and effectors with feedback loop
        feedbackLoop.setProbe(probe);
        feedbackLoop.setEffector(effector);

        // StartFeedback loop
        feedbackLoop.start();

        ArrayList<QoS> result = networkMgmt.getNetworkQoS(numOfRuns);

        System.out.println("Run, PacketLoss, EnergyConsumption");
        result.forEach(qos -> System.out.println(qos));

        CsvFileWriter.saveQoS(result, feedbackLoop.getId());

    }

    public static void main(String[] args) {
        SimpleAdaptation client = new SimpleAdaptation(DeltaIoTSimulator.NUM_OF_RUNS);
        client.start();
    }

    public Simulator getSimulator() {
        return networkMgmt.getSimulator();
    }
}
