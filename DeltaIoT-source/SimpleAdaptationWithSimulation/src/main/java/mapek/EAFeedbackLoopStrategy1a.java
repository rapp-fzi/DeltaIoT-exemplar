package mapek;

import deltaiot.client.Effector;
import deltaiot.client.Probe;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import util.IMoteWriter;

public class EAFeedbackLoopStrategy1a extends FeedbackLoop {

    private static int CHANGE_POWER_VALUE = 1;
    private static int CHANGE_DIST_VALUE = 10; // original value from Paper: 10.0
    private static int UNIFORM_DIST_VALUE = 50;
    private static int TOTAL_DIST_VALUE = 100;

    private static int POWER_MIN = 0;
    private static int POWER_MAX = 15;

    public EAFeedbackLoopStrategy1a(int numOfRuns, Probe probe, Effector effector, IMoteWriter moteWriter) {
        super(numOfRuns, probe, effector, moteWriter);
    }

    @Override
    boolean analyzeLinkSettings() {
        // analyze all links for possible adaptation options
        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                if (link.getSNR() > 0 && link.getPower() > POWER_MIN
                        || link.getSNR() < 0 && link.getPower() < POWER_MAX) {
                    return true;
                }
            }
            if (mote.getLinks()
                .size() == 2) {
                if (mote.getLinks()
                    .get(0)
                    .getPower() != mote.getLinks()
                        .get(1)
                        .getPower())
                    return true;
            }
        }
        return false;
    }

    @Override
    void planning() {

        // Go through all links
        boolean powerChanging = false;
        Link left, right;
        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                powerChanging = false;
                if (link.getSNR() > 0 && link.getPower() > POWER_MIN) {
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - CHANGE_POWER_VALUE));
                    powerChanging = true;
                } else if (link.getSNR() < 0 && link.getPower() < POWER_MAX) {
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() + CHANGE_POWER_VALUE));
                    powerChanging = true;
                }
            }
            if (mote.getLinks()
                .size() == 2 && powerChanging == false) {
                left = mote.getLinks()
                    .get(0);
                right = mote.getLinks()
                    .get(1);
                if (left.getPower() != right.getPower()) {
                    // If distribution of all links is 100 then change it to 50
                    // 50
                    if (left.getDistribution() == TOTAL_DIST_VALUE && right.getDistribution() == TOTAL_DIST_VALUE) {
                        left.setDistribution(UNIFORM_DIST_VALUE);
                        right.setDistribution(UNIFORM_DIST_VALUE);
                    }
                    if (left.getPower() > right.getPower() && left.getDistribution() < TOTAL_DIST_VALUE) {
                        steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() + CHANGE_DIST_VALUE));
                        steps.add(
                                new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() - CHANGE_DIST_VALUE));
                    } else if (right.getDistribution() < TOTAL_DIST_VALUE) {
                        steps.add(
                                new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() + CHANGE_DIST_VALUE));
                        steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() - CHANGE_DIST_VALUE));
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
        return "DeltaIoTEAStrategy1aReconfigurationStrategy";
    }
}
