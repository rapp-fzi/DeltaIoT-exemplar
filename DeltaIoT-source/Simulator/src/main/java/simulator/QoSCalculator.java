package simulator;

import java.util.List;

public class QoSCalculator {

    public double calcEnergyConsumptionAverage(List<QoS> qos) {
        qos.stream()
            .filter(e -> !QoS.RANGE_ENERGY_CONSUMPTION.contains(e.getEnergyConsumption()))
            .findFirst()
            .ifPresent(e -> {
                throw new IllegalArgumentException(String.format("energy consumtion out of bounds: %s (%s)",
                        e.getEnergyConsumption(), QoS.RANGE_ENERGY_CONSUMPTION));
            });

        double average = qos.stream()
            .mapToDouble(QoS::getEnergyConsumption)
            .average()
            .orElse(Double.NaN);
        return average;
    }

    public double calcPacketLossAverage(List<QoS> qos) {
        qos.stream()
            .filter(e -> !QoS.RANGE_PACKET_LOSS.contains(e.getPacketLoss()))
            .findFirst()
            .ifPresent(e -> {
                throw new IllegalArgumentException(String.format("packet loss out of bounds: %s (%s)",
                        e.getPacketLoss(), QoS.RANGE_PACKET_LOSS));
            });

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
