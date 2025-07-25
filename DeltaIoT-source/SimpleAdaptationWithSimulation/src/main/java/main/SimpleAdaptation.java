package main;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.client.ISimulationResult;
import deltaiot.client.ISimulationRunner;
import deltaiot.client.SimulationClient;
import deltaiot.client.SimulationResult;
import mapek.IAdaptionStrategy;
import simulator.QoS;
import simulator.Simulator;

public class SimpleAdaptation implements ISimulationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAdaptation.class);

    private final SimulationClient networkMgmt;
    private final IAdaptionStrategy feedbackLoop;

    public SimpleAdaptation(SimulationClient networkMgmt, IAdaptionStrategy feedbackLoop) {
        this.networkMgmt = networkMgmt;
        this.feedbackLoop = feedbackLoop;
    }

    @Override
    public ISimulationResult run() throws IOException {
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
