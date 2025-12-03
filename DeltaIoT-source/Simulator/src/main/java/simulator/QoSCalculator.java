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
        DoubleSummaryStatistics energyStats = qos.stream()
            .map(e -> normalize(e.getEnergyConsumption(), QoS.RANGE_ENERGY_CONSUMPTION))
            .collect(Collectors.summarizingDouble((Double::doubleValue)));
        DoubleSummaryStatistics packetStats = qos.stream()
            .map(e -> normalize(e.getPacketLoss(), QoS.RANGE_PACKET_LOSS))
            .collect(Collectors.summarizingDouble((Double::doubleValue)));

        double energyConsumptionAverage = energyStats.getAverage();
        double packetLossAverage = packetStats.getAverage();
        return (energyConsumptionAverage + packetLossAverage) / 2;
    }

    double normalize(double value, Range<Double> range) {
        if (value > range.getMaximum()) {
            return 1;
        }

        if (value < range.getMinimum()) {
            return 0;
        }

        return (value - range.getMinimum()) / (range.getMaximum() - range.getMinimum());
    }
}
