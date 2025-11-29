package simulator;

import org.apache.commons.lang3.Range;

public class QoS {
    public final static Range<Double> RANGE_PACKET_LOSS = Range.of(0.025, 0.2);
    public final static Range<Double> RANGE_ENERGY_CONSUMPTION = Range.of(10.0, 26.0);

    private final double packetLoss;
    private final double powerConsumption;
    private final int period;

    public QoS(int period, double packetLoss, double powerConsumption) {
        this.period = period;
        this.packetLoss = packetLoss;
        this.powerConsumption = powerConsumption;
    }

    public double getPacketLoss() {
        return packetLoss;
    }

    public double getEnergyConsumption() {
        return powerConsumption;
    }

    public int getPeriod() {
        return period;
    }

    @Override
    public String toString() {
        return String.format("%s, %f, %f", period, packetLoss, powerConsumption);
    }
}
