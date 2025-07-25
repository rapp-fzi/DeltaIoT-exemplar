package deltaiot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import domain.Gateway;
import domain.Link;
import domain.Mote;
import domain.Node;
import simulator.ISimulatorConfig;
import simulator.Simulator;
import simulator.SimulatorFactory;

public class DeltaIoTSimulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaIoTSimulator.class);

    public final static int NUM_OF_RUNS = 96;

    final Lock lock = new ReentrantLock();
    final Condition adaptationCompleted = lock.newCondition();
    private final static int GATEWAY_ID = 1;
    private final static int PORT = 9888;

    Simulator simul;

    public DeltaIoTSimulator(int numOfRuns) {
        ISimulatorConfig config = null;
        this.simul = SimulatorFactory.createExperimentSimulator(config);
    }

    public void run() {
        try {
            LOGGER.info("--START--");

            // Create socket
            ServerSocket serverSocket = new ServerSocket(PORT);
            LOGGER.info("Waiting to connect...");
            Socket socket = serverSocket.accept();
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream output = new PrintStream(socket.getOutputStream());
            LOGGER.info("Connected.");

            // Create thread that listens for messages from the client
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String fromFeedbackLoop = null;
                        while ((fromFeedbackLoop = input.readLine()) != null) {
                            applyNewSettings(fromFeedbackLoop);
                        }
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            }).start();

            // Do logic
            LOGGER.info("--SIMULATION--");
            List<String> printsBackup = new ArrayList<>();
            for (int i = 0; i < 96; ++i) {
                LOGGER.info("Run... {}", i);

                // Do emulation
                simul.doSingleRun();
                String print = simul.getGatewayWithId(GATEWAY_ID)
                    .toString();
                printsBackup.add(print);
                LOGGER.info(print);

                // Send info to client
                String infoToSend = createInfoToSend();
                LOGGER.info("To FeedbackLoop: {}", infoToSend);
                output.println(infoToSend);

                // Wait for client response
                lock.lock();
                try {
                    adaptationCompleted.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }

            // Report
            LOGGER.info("--FINAL REPORT--");
            for (String print : printsBackup) {
                LOGGER.info(print);
            }

            // Cleanup
            input.close();
            output.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String createInfoToSend() {
        deltaiot.DeltaIoT deltaIoT = new deltaiot.DeltaIoT();

        Gateway gw = simul.getGatewayWithId(GATEWAY_ID);
        deltaIoT.setPacketLoss(gw.calculatePacketLoss());
        deltaIoT.setPower(gw.getPowerConsumed());

        HashMap<Integer, deltaiot.services.Mote> afMotes = new HashMap<>();
        for (Mote mote : gw.getView()) {
            int moteid = mote.getId();
            deltaiot.services.Mote afMote = getAfMote(mote, simul);
            afMotes.put(moteid, afMote);
        }
        deltaIoT.Motes = afMotes;

        return deltaIoT.toJson();
    }

    public static deltaiot.services.Mote getAfMote(Mote mote, Simulator simul) {
        int moteid = mote.getId();
        int load = mote.getLoad();
        double battery = mote.getBatteryRemaining();
        int parents = mote.getLinks()
            .size();
        int dataProbability = (int) Math.round(mote.getActivationProbability()
            .get(simul.getRunInfo()
                .getRunNumber()) * 100);

        List<deltaiot.services.Link> afLinks = new LinkedList<>();
        for (Link link : mote.getLinks()) {
            double latency = 0; // unused
            int power = link.getPowerNumber();
            int packetLoss = link.calculatePacketLoss(simul.getRunInfo());
            int source = link.getFrom()
                .getId();
            int dest = link.getTo()
                .getId();
            double sNR = link.getSnrEquation()
                .getSNR(link.getPowerNumber());
            int distribution = link.getDistribution();
            int sF = link.getSfTimeNumber();

            deltaiot.services.Link afLink = new deltaiot.services.Link(latency, power, packetLoss, source, dest, sNR,
                    distribution, sF);
            afLinks.add(afLink);
        }

        deltaiot.services.Mote afMote = new deltaiot.services.Mote(moteid, load, battery, parents, dataProbability,
                afLinks);
        return afMote;
    }

    public void applyNewSettings(String msgSettings) {
        LOGGER.info("Message Received from Client:{}", msgSettings);
        if (msgSettings.equalsIgnoreCase("NoAdaptationRequired")
                || msgSettings.equalsIgnoreCase("AdaptationCompleted")) {
            lock.lock();
            try {

                synchronized (adaptationCompleted) {
                    adaptationCompleted.signal();
                }
            } finally {
                lock.unlock();
            }
        } else {
            Gson gson = new Gson();
            deltaiot.services.Mote afMote = gson.fromJson(msgSettings, new TypeToken<deltaiot.services.Mote>() {
            }.getType());
            for (deltaiot.services.Link afLink : afMote.getLinks()) {

                Mote source = simul.getMoteWithId(afMote.getMoteid());
                Node destination = simul.getNodeWithId(afLink.getDest());
                Link actualLink = source.getLinkTo(destination);

                if (actualLink.getDistribution() != afLink.getDistribution()) {
                    actualLink.setDistribution(afLink.getDistribution());
                    LOGGER.info("Link distribution adapted: {}", actualLink);
                }
                if (actualLink.getPowerNumber() != afLink.getPower()) {
                    actualLink.setPowerNumber(afLink.getPower());
                    LOGGER.info("Link power adapted:        {}", actualLink);
                }
            }
        }
    }

}
