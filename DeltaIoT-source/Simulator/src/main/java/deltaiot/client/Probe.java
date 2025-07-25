package deltaiot.client;

import java.util.List;

import deltaiot.services.Mote;
import simulator.QoS;

public interface Probe {

    public List<Mote> getAllMotes();

    public double getMoteEnergyLevel(int moteId);

    public double getMoteTrafficLoad(int moteId);

    public int getLinkPowerSetting(int src, int dest);

    public int getLinkSpreadingFactor(int src, int dest);

    public double getLinkSignalNoise(int src, int dest);

    public double getLinkDistributionFactor(int src, int dest);

    public List<QoS> getNetworkQoS(int period);
}
