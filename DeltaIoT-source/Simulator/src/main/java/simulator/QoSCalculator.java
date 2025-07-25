package simulator;

import java.util.List;

public class QoSCalculator {

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

    public double calcScore(List<QoS> qos) {
        double energyConsumptionAverage = calcEnergyConsumptionAverage(qos);
        double packetLossAverage = calcPacketLossAverage(qos);
        return (energyConsumptionAverage + packetLossAverage) / 2;
    }

}
