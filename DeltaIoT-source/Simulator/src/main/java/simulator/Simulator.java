package simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import domain.Gateway;
import domain.Mote;
import domain.Node;
import domain.RunInfo;

public class Simulator {

    private final int numOfRuns;

    private List<Mote> motes = new ArrayList<>();
    private List<Gateway> gateways = new ArrayList<>();
    private List<Integer> turnOrder = new ArrayList<>();
    private RunInfo runInfo = new RunInfo();
    private List<QoS> qosValues = new ArrayList<>();

    // Constructor
    public Simulator(int numOfRuns) {
        this.numOfRuns = numOfRuns;
    }

    // Creation API

    public void addMotes(Mote... motes) {
        Collections.addAll(this.motes, motes);
    }

    public void addGateways(Gateway... gateways) {
        Collections.addAll(this.gateways, gateways);
    }

    public void setTurnOrder(Mote... motes) {
        Integer[] ids = new Integer[motes.length];
        for (int i = 0; i < motes.length; ++i) {
            ids[i] = motes[i].getId();
        }
        setTurnOrder(ids);
    }

    public void setTurnOrder(Integer... ids) {
        this.turnOrder = Arrays.asList(ids);
    }

    // Simulation API

    /**
     * Do a single simulation run. This will simulate the sending of packets through the network to
     * the gateways. Each gateway will aggregate information about packet-loss and
     * power-consumption. To get this information, use gateway.calculatePacketLoss and
     * gateway.getPowerConsumed respectively.
     */
    public void doSingleRun() {
        // Reset the gateways aggregated values, so we can start a new window to see packet loss and
        // power consumption
        resetGatewaysAggregatedValues();

        // Do the actual run, this will give all motes a turn
        // Give each mote a turn, in the given order
        for (Integer id : turnOrder) {
            Mote mote = getMoteWithId(id);
            // Let mote handle its turn
            mote.handleTurn(runInfo); // return value doesn't include packets send for other motes,
                                      // only its own packets
        }

        // QoS
        QoS qos = new QoS();
        qos.setEnergyConsumption(gateways.get(0)
            .getPowerConsumed());
        qos.setPacketLoss(gateways.get(0)
            .calculatePacketLoss());
        qos.setPeriod("" + runInfo.getRunNumber());
        qosValues.add(qos);

        // Increase run number
        runInfo.incrementRunNumber();
    }

    private void resetGatewaysAggregatedValues() {
        // Reset gateways' packetstore and expected packet count, so the packetloss for this run can
        // be calculated easily
        // Also reset the consumed power, so this is correctly aggregated for this run
        for (Gateway gateway : gateways) {
            gateway.resetPacketStoreAndExpectedPacketCount();
            gateway.resetPowerConsumed();
        }
    }

    // Alteration and inspection API

    public Mote getMoteWithId(int id) {
        for (Mote mote : motes) {
            if (mote.getId() == id)
                return mote;
        }
        return null;
    }

    public Gateway getGatewayWithId(int id) {
        for (Gateway gw : gateways) {
            if (gw.getId() == id)
                return gw;
        }
        return null;
    }

    public List<Integer> getTurnOrder() {
        return Collections.unmodifiableList(turnOrder);
    }

    /**
     * Gets the Node with a specified id if one exists This can be both a Mote or a Gateway
     * 
     * @param id
     *            The id
     * @return The node with the given id (either a mote or gateway) if one exists (null otherwise)
     */
    public Node getNodeWithId(int id) {
        Mote mote = getMoteWithId(id);
        if (mote == null) {
            Gateway gw = getGatewayWithId(id);
            return gw;
        } else
            return mote;
    }

    public List<Gateway> getGateways() {
        return Collections.unmodifiableList(gateways);
    }

    public List<Mote> getMotes() {
        return Collections.unmodifiableList(motes);
    }

    public RunInfo getRunInfo() {
        return runInfo;
    }

    public List<QoS> getQosValues() {
        return qosValues;
    }

    public int getNumOfRuns() {
        return numOfRuns;
    }
}
