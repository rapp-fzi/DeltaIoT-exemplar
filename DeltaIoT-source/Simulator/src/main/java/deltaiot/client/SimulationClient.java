package deltaiot.client;

import java.util.ArrayList;
import java.util.List;

import deltaiot.DeltaIoTSimulator;
import deltaiot.services.LinkSettings;
import domain.Link;
import domain.Mote;
import domain.Node;
import simulator.QoS;
import simulator.Simulator;

public class SimulationClient implements Probe, Effector {

    private final Simulator simulator;

    public SimulationClient(Simulator simulator) {
        this.simulator = simulator;
    }

    @Override
    public ArrayList<deltaiot.services.Mote> getAllMotes() {
        simulator.doSingleRun();
        List<Mote> motes = simulator.getMotes();
        ArrayList<deltaiot.services.Mote> afMotes = new ArrayList<>();
        for (Mote mote : motes) {
            afMotes.add(DeltaIoTSimulator.getAfMote(mote, simulator));
        }
        return afMotes;
    }

    private Mote getMote(int moteId) {
        return simulator.getMoteWithId(moteId);
    }

    private domain.Link getLink(int src, int dest) {
        Mote from = simulator.getMoteWithId(src);
        Node to = simulator.getNodeWithId(dest);
        domain.Link link = from.getLinkTo(to);
        return link;
    }

    @Override
    public double getMoteEnergyLevel(int moteId) {
        return getMote(moteId).getBatteryRemaining();
    }

    @Override
    public double getMoteTrafficLoad(int moteId) {
        return getMote(moteId).getActivationProbability()
            .get(simulator.getRunInfo()
                .getRunNumber());
    }

    @Override
    public int getLinkPowerSetting(int src, int dest) {
        return getLink(src, dest).getPowerNumber();
    }

    @Override
    public int getLinkSpreadingFactor(int src, int dest) {
        return getLink(src, dest).getSfTimeNumber();
    }

    @Override
    public double getLinkSignalNoise(int src, int dest) {
        return getLink(src, dest).getSRN(simulator.getRunInfo());
    }

    @Override
    public double getLinkDistributionFactor(int src, int dest) {
        return getLink(src, dest).getDistribution();
    }

//	@Override
//	public void setLinkSF(int src, int dest, int sf) {
//		getLink(src, dest).setSfTimeNumber(sf);
//	}
//
//	@Override
//	public void setLinkPower(int src, int dest, int power) {
//		getLink(src, dest).setPowerNumber(power);
//	}
//
//	@Override
//	public void setLinkDistributionFactor(int src, int dest, int distributionFactor) {
//		getLink(src, dest).setDistribution(distributionFactor);
//	}

    @Override
    public void setMoteSettings(int moteId, List<LinkSettings> linkSettings) {
        Mote mote = getMote(moteId);
        Node node;
        Link link;
        for (LinkSettings setting : linkSettings) {
            node = simulator.getNodeWithId(setting.getDest());
            link = mote.getLinkTo(node);
            link.setPowerNumber(setting.getPowerSettings());
            link.setDistribution(setting.getDistributionFactor());
            link.setSfTimeNumber(setting.getSpreadingFactor());
        }
    }

    @Override
    public void setDefaultConfiguration() {
        List<Mote> motes = simulator.getMotes();
        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                link.setDistribution(100);
                link.setPowerNumber(15);
                link.setSfTimeNumber(11);
            }
        }

    }

    @Override
    public List<QoS> getNetworkQoS(int period) {
        List<QoS> qosOrigList = simulator.getQosValues();
        int qosSize = qosOrigList.size();

        if (period >= qosSize)
            return qosOrigList;

        int startIndex = qosSize - period;

        ArrayList<QoS> newList = new ArrayList<>();

        for (int i = startIndex; i < qosSize; i++) {
            newList.add(qosOrigList.get(i));
        }
        return newList;
    }

    public Probe getProbe() {
        return this;
    }

    public Effector getEffector() {
        return this;
    }

    public Simulator getSimulator() {
        return this.simulator;
    }
}
