package simulator;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;

public class QoSCalculator {
    public DoubleSummaryStatistics calcEnergyConsumptionStatistics(List<QoS> qos) {
        DoubleSummaryStatistics energyStats = qos.stream()
            .map(e -> e.getEnergyConsumption())
            .collect(Collectors.summarizingDouble((Double::doubleValue)));
        return energyStats;
    }

    public DoubleSummaryStatistics calcPacketLossStatistics(List<QoS> qos) {
        DoubleSummaryStatistics packetStats = qos.stream()
            .map(e -> e.getPacketLoss())
            .collect(Collectors.summarizingDouble((Double::doubleValue)));
        return packetStats;
    }

    public double calcEnergyConsumptionAverage(List<QoS> qos) {
        DoubleSummaryStatistics energyStats = calcEnergyConsumptionStatistics(qos);
        return energyStats.getAverage();
    }

    public double calcPacketLossAverage(List<QoS> qos) {
        DoubleSummaryStatistics packetStats = calcPacketLossStatistics(qos);
        return packetStats.getAverage();
    }

    public double averageScore(List<QoS> qos) {
        double energyConsumptionAverage = calcEnergyConsumptionAverage(qos);
        double packetLossAverage = calcPacketLossAverage(qos);
        return (energyConsumptionAverage + packetLossAverage) / 2;
    }

    public double normalizedScore(List<QoS> qos) {
        double score = qos.stream()
            .mapToDouble(e -> normalizedScore(e))
            .sum();
        return score;
    }

    private double normalizedScore(QoS qos) {
        double normalizedEnergyConsumption = normalize(qos.getEnergyConsumption(), QoS.RANGE_ENERGY_CONSUMPTION);
        double normalizedPacketLoss = normalize(qos.getPacketLoss(), QoS.RANGE_PACKET_LOSS);
        return (normalizedEnergyConsumption + normalizedPacketLoss) / 2;
    }

    double normalize_(double value, Range<Double> range) {
        if (value > range.getMaximum()) {
            return 1;
        }

        if (value < range.getMinimum()) {
            return 0;
        }

        return (value - range.getMinimum()) / (range.getMaximum() - range.getMinimum());
    }

    double normalize(double value, Range<Double> range) {
        if (value > range.getMaximum()) {
            return 0;
        }

        if (value < range.getMinimum()) {
            return 1;
        }

        return (1 / (range.getMaximum() - range.getMinimum())) * (range.getMaximum() - value);
    }
}
