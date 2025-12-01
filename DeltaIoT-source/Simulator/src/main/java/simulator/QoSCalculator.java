package simulator;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

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
        double average = qos.stream()
            .mapToDouble(QoS::getEnergyConsumption)
            .average()
            .orElse(Double.NaN);
        return average;
    }

    public double calcPacketLossAverage(List<QoS> qos) {
        double average = qos.stream()
            .mapToDouble(QoS::getPacketLoss)
            .average()
            .orElse(Double.NaN);
        return average;
    }

    public double averageScore(List<QoS> qos) {
        double energyConsumptionAverage = calcEnergyConsumptionAverage(qos);
        double packetLossAverage = calcPacketLossAverage(qos);
        return (energyConsumptionAverage + packetLossAverage) / 2;
    }

}
