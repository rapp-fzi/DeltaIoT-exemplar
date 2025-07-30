package simulator;

public class QoS {
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
