package simulator;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QoSValidator {
    public void validate(List<QoS> qos) {
        Optional<String> energyResult = validateEnergy(qos);
        Optional<String> packetLossresult = validatePacketLoss(qos);

        if (energyResult.isEmpty() && packetLossresult.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (energyResult.isPresent()) {
            sb.append(energyResult.get());
        }
        if (energyResult.isPresent() && packetLossresult.isPresent()) {
            sb.append(" and ");
        }
        if (packetLossresult.isPresent()) {
            sb.append(packetLossresult.get());
        }

        throw new IllegalArgumentException(sb.toString());
    }

    private Optional<String> validateEnergy(List<QoS> qos) {
        DoubleSummaryStatistics energyStats = qos.stream()
            .map(e -> e.getEnergyConsumption())
            .collect(Collectors.summarizingDouble((Double::doubleValue)));
        if (QoS.RANGE_ENERGY_CONSUMPTION.contains(energyStats.getMin())
                && QoS.RANGE_ENERGY_CONSUMPTION.contains(energyStats.getMax())) {
            return Optional.empty();
        }

        String errorMessage = String.format("energy consumtion min/max out of bounds %s: %s/%s",
                QoS.RANGE_ENERGY_CONSUMPTION, energyStats.getMin(), energyStats.getMax());
        return Optional.of(errorMessage);
    }

    private Optional<String> validatePacketLoss(List<QoS> qos) {
        DoubleSummaryStatistics packetStats = qos.stream()
            .map(e -> e.getPacketLoss())
            .collect(Collectors.summarizingDouble((Double::doubleValue)));
        if (QoS.RANGE_PACKET_LOSS.contains(packetStats.getMin())
                && QoS.RANGE_PACKET_LOSS.contains(packetStats.getMax())) {
            return Optional.empty();
        }
        String errorMessage = String.format("packet loss min/max out of bounds %s: %f/%f", QoS.RANGE_PACKET_LOSS,
                packetStats.getMin(), packetStats.getMax());
        return Optional.of(errorMessage);
    }
}
