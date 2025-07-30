package util;

import java.util.List;

import simulator.QoS;

public class QoSResult {
    private final String strategyName;
    private final List<QoS> qosEntries;
    private final double energyConsumptionAverage;
    private final double packetLossAverage;
    private final double score;

    public QoSResult(String strategyName, List<QoS> qosEntries, double energyConsumptionAverage,
            double packetLossAverage, double score) {
        this.strategyName = strategyName;
        this.qosEntries = qosEntries;
        this.energyConsumptionAverage = energyConsumptionAverage;
        this.packetLossAverage = packetLossAverage;
        this.score = score;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public List<QoS> getQosEntries() {
        return qosEntries;
    }

    public double getEnergyConsumptionAverage() {
        return energyConsumptionAverage;
    }

    public double getPacketLossAverage() {
        return packetLossAverage;
    }

    public double getScore() {
        return score;
    }
}
