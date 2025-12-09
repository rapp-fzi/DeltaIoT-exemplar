package mapek.strategy;

import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import util.IMoteWriter;

public class EAFeedbackLoopStrategy1b extends EAFeedbackLoopStrategy1a {

    private static int CHANGE_DIST_VALUE = 10; // original value from Paper: 10.0
    private static int UNIFORM_DIST_VALUE = 50;

    public EAFeedbackLoopStrategy1b(SimulationClient networkMgmt, IMoteWriter moteWriter,
            StrategyConfigurationEAStrategy1b configuration) {
        super(networkMgmt, moteWriter, configuration);
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
                    if (left.getPower() > right.getPower()) {
                        if (right.getDistribution() <= 100 - CHANGE_DIST_VALUE) {
                            steps.add(new PlanningStep(Step.CHANGE_DIST, right,
                                    right.getDistribution() + CHANGE_DIST_VALUE));
                            steps.add(new PlanningStep(Step.CHANGE_DIST, left,
                                    left.getDistribution() - CHANGE_DIST_VALUE));
                        }
                    } else {
                        if (left.getDistribution() <= 100 - CHANGE_DIST_VALUE) {
                            steps.add(new PlanningStep(Step.CHANGE_DIST, left,
                                    left.getDistribution() + CHANGE_DIST_VALUE));
                            steps.add(new PlanningStep(Step.CHANGE_DIST, right,
                                    right.getDistribution() - CHANGE_DIST_VALUE));
                        }
                    }
                }
            }
        }

        if (steps.size() > 0) {
            execution();
        }
    }

    @Override
    public String getId() {
        return "EAFeedbackLoopStrategy1b";
    }
}
