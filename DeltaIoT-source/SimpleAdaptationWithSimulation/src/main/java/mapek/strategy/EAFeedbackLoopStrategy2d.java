package mapek.strategy;

import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import util.IMoteWriter;

public class EAFeedbackLoopStrategy2d extends EAFeedbackLoopStrategy1c {

    private final StrategyConfigurationEAStrategy2d config;

    public EAFeedbackLoopStrategy2d(SimulationClient networkMgmt, IMoteWriter moteWriter,
            StrategyConfigurationEAStrategy2d configuration) {
        super(networkMgmt, moteWriter, configuration);
        this.config = configuration;
    }

    @Override
    void planning() {

        // Go through all links
        boolean powerChanging = false;
        Link left, right;
        for (Mote mote : motes) {
            powerChanging = false;
            for (Link link : mote.getLinks()) {
                int linkNumber = getLinkNumber(link);
                int changePowerValue = getChangePowerValue(linkNumber);

                if (link.getSNR() > 0 && link.getPower() > 0) {
                    int maxChange = Math.min(changePowerValue, link.getPower());
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - maxChange));
                    powerChanging = true;
                } else if (link.getSNR() < 0 && link.getPower() < 15) {
                    int maxChange = Math.min(changePowerValue, 15 - link.getPower());
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() + maxChange));
                    powerChanging = true;
                }
            }

            if (mote.getLinks()
                .size() == 2 && planDistribution(powerChanging)) {
                left = mote.getLinks()
                    .get(0);
                right = mote.getLinks()
                    .get(1);
                if (left.getPower() != right.getPower()) {
                    // If distribution of all links is 100 then change it to 50
                    // 50
                    if (left.getDistribution() == 100 && right.getDistribution() == 100) {
                        left.setDistribution(UNIFORM_DIST_VALUE);
                        right.setDistribution(UNIFORM_DIST_VALUE);
                    }

                    // Optimize distribution factor of the links such that the messages are routed
                    // to the link that uses less power.
                    int changeDistValue = getChangeDistValue(left, right);
                    if (left.getPower() > right.getPower()) {
                        if (right.getDistribution() <= 100 - changeDistValue) {
                            steps.add(new PlanningStep(Step.CHANGE_DIST, right,
                                    right.getDistribution() + changeDistValue));
                            steps.add(
                                    new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() - changeDistValue));
                        }
                    } else {
                        if (left.getDistribution() <= 100 - changeDistValue) {
                            steps.add(
                                    new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() + changeDistValue));
                            steps.add(new PlanningStep(Step.CHANGE_DIST, right,
                                    right.getDistribution() - changeDistValue));
                        }
                    }
                }
            }
        }

        if (steps.size() > 0) {
            execution();
        }
    }

    private int getChangeDistValue(Link left, Link right) {
        // CHANGE_DIST_VALUE_7_8
        if ((left.getDest() == 2) && (right.getDest() == 3)) {
            return config.CHANGE_DIST_VALUE_7_8;
        }

        // CHANGE_DIST_VALUE_15_16
        if ((left.getDest() == 6) && (right.getDest() == 5)) {
            return config.CHANGE_DIST_VALUE_15_16;
        }

        // CHANGE_DIST_VALUE_5_6
        if ((left.getDest() == 7) && (right.getDest() == 3)) {
            return config.CHANGE_DIST_VALUE_5_6;
        }

        throw new RuntimeException(String.format("unknown Link left: %d->%d right: %d->%d", left.getSource(),
                left.getDest(), right.getSource(), right.getDest()));
    }

    @Override
    public String getId() {
        return "EAFeedbackLoopStrategy2d";
    }
}
