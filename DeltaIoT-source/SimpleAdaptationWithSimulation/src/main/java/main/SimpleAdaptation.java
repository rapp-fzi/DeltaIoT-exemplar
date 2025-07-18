package main;

import java.util.ArrayList;

import deltaiot.DeltaIoTSimulator;
import deltaiot.client.Effector;
import deltaiot.client.Probe;
import deltaiot.client.SimulationClient;
import mapek.EAStrategy1aFeedbackLoop;
import mapek.FeedbackLoop;
import simulator.QoS;
import simulator.Simulator;
import util.CsvFileWriter;

public class SimpleAdaptation {

    SimulationClient networkMgmt;

    public void start() {

        // Create a simulation client object
        networkMgmt = new SimulationClient();

        // Create Feedback loop
        FeedbackLoop feedbackLoop = new EAStrategy1aFeedbackLoop();

        // get probe and effectors
        Probe probe = networkMgmt.getProbe();
        Effector effector = networkMgmt.getEffector();

        // Connect probe and effectors with feedback loop
        feedbackLoop.setProbe(probe);
        feedbackLoop.setEffector(effector);

        // StartFeedback loop
        feedbackLoop.start();

        ArrayList<QoS> result = networkMgmt.getNetworkQoS(DeltaIoTSimulator.NUM_OF_RUNS);

        System.out.println("Run, PacketLoss, EnergyConsumption");
        result.forEach(qos -> System.out.println(qos));

        CsvFileWriter.saveQoS(result, feedbackLoop.getId());

    }

    public static void main(String[] args) {
        SimpleAdaptation client = new SimpleAdaptation();
        client.start();
    }

    public Simulator getSimulator() {
        return networkMgmt.getSimulator();
    }
}
