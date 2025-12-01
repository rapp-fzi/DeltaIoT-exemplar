package simulator;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

public class QoSCalculator {

    public double calcEnergyConsumptionAverage(List<QoS> qos) {
        DoubleSummaryStatistics stats = qos.stream()
            .map(e -> e.getEnergyConsumption())
            .collect(Collectors.summarizingDouble((Double::doubleValue)));
        if (!QoS.RANGE_ENERGY_CONSUMPTION.contains(stats.getMin())
                || !QoS.RANGE_ENERGY_CONSUMPTION.contains(stats.getMax())) {
            throw new IllegalArgumentException(String.format("energy consumtion min/max out of bounds %s: %s/%s",
                    QoS.RANGE_ENERGY_CONSUMPTION, stats.getMin(), stats.getMax()));
        }

        double average = qos.stream()
            .mapToDouble(QoS::getEnergyConsumption)
            .average()
            .orElse(Double.NaN);
        return average;
    }

    public double calcPacketLossAverage(List<QoS> qos) {
        DoubleSummaryStatistics stats = qos.stream()
            .map(e -> e.getPacketLoss())
            .collect(Collectors.summarizingDouble((Double::doubleValue)));
        if (!QoS.RANGE_PACKET_LOSS.contains(stats.getMin()) || !QoS.RANGE_PACKET_LOSS.contains(stats.getMax())) {
            throw new IllegalArgumentException(String.format("packet loss min/max out of bounds %s: %f/%f",
                    QoS.RANGE_PACKET_LOSS, stats.getMin(), stats.getMax()));
        }

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
