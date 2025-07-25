package mapek.strategy;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import simulator.QoS;
import util.ICSVWriter;

public class QualityBasedFeedbackLoop extends FeedbackLoop {

    private final static double LOWER_PACKET_LOSS = 0.1;
    // private final static double LOWER_ENERGY_CONSUMPTION = 26;
    private final static double LOWER_ENERGY_CONSUMPTION = 18;
    // private final static double LOWER_ENERGY_CONSUMPTION = 10;

    private final SimulationClient networkMgmt;

    public QualityBasedFeedbackLoop(SimulationClient networkMgmt, ICSVWriter csvWriter) {
        super(networkMgmt, csvWriter);
        this.networkMgmt = networkMgmt;
    }

    @Override
    public String getId() {
        return "QualityBasedDeltaIoTStrategy";
    }

    @Override
    public void analysis() {
        if (isAdaptationRequired()) {
            planning();
        }
    }

    @Override
    public void planning() {
        if (isEnergyConsumptionViolated()) {
            planEnergyConsumption();
        } else {
            planPacketLoss();
        }

        if (steps.isEmpty() == false) {
            execution();
        }
    }

    private void planPacketLoss() {
        increaseTransmissionPowerLocally();
        increaseDistributionFactorsLocally();
    }

    private void planEnergyConsumption() {
        decreaseTransmissionPowerLocally();
        decreaseDistributionFactorsLocally();
    }

    private void increaseTransmissionPowerLocally() {
        for (Link each : filterLinksWithNegativSNR()) {
            if (each.getDistribution() > 0 && each.getPower() < 15) {
                steps.add(new PlanningStep(Step.CHANGE_POWER, each, each.getPower() + 1));
            }
        }
    }

    private void decreaseTransmissionPowerLocally() {
        for (Link each : filterLinksWithPositivSNR()) {
            if (each.getDistribution() > 0 && each.getPower() > 0) {
                steps.add(new PlanningStep(Step.CHANGE_POWER, each, each.getPower() - 1));
            }
        }
    }

    private void increaseDistributionFactorsLocally() {
        for (Mote each : filterMotesWithTwoLinks()) {
            Link left = each.getLinks()
                .get(0);
            Link right = each.getLinks()
                .get(1);
            if (left.getSNR() > right.getSNR() && left.getDistribution() < 100) {
                steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() + 10));
                steps.add(new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() - 10));
            } else if (right.getDistribution() < 100) {
                steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() - 10));
                steps.add(new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() + 10));
            }
        }
    }

    private void decreaseDistributionFactorsLocally() {
        for (Mote each : filterMotesWithTwoLinks()) {
            Link left = each.getLinks()
                .get(0);
            Link right = each.getLinks()
                .get(1);
            if (left.getPower() != right.getPower()) {
                if (left.getPower() > right.getPower() && right.getDistribution() < 100) {
                    steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() - 10));
                    steps.add(new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() + 10));
                } else if (left.getDistribution() < 100) {
                    steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() + 10));
                    steps.add(new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() - 10));
                }
            }
        }
    }

    private List<Link> filterLinksWithPositivSNR() {
        return filterLinksWith(link -> link.getSNR() >= 0);
    }

    private List<Link> filterLinksWithNegativSNR() {
        return filterLinksWith(link -> link.getSNR() < 0);
    }

    private List<Link> filterLinksWith(Predicate<Link> criterion) {
        return motes.stream()
            .flatMap(each -> each.getLinks()
                .stream())
            .filter(criterion)
            .collect(Collectors.toList());
    }

    private List<Mote> filterMotesWithTwoLinks() {
        return motes.stream()
            .filter(each -> each.getLinks()
                .size() > 1)
            .collect(Collectors.toList());
    }

    private boolean isAdaptationRequired() {
        return isPacketLossViolated() || isEnergyConsumptionViolated();
    }

    private boolean isPacketLossViolated() {
        QoS qos = getCurrentQoS();
        return qos.getPacketLoss() > LOWER_PACKET_LOSS;
    }

    private boolean isEnergyConsumptionViolated() {
        QoS qos = getCurrentQoS();
        return qos.getEnergyConsumption() > LOWER_ENERGY_CONSUMPTION;
    }

    private QoS getCurrentQoS() {
        List<QoS> collectedQoS = networkMgmt.getNetworkQoS(Integer.MAX_VALUE);
        return collectedQoS.get(collectedQoS.size() - 1);
    }

}
