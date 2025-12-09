package mapek.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import util.IMoteWriter;

class EADefaultFeedbackLoop extends FeedbackLoop {
    private static final Logger LOGGER = LoggerFactory.getLogger(EADefaultFeedbackLoop.class);

    private static int CHANGE_POWER_VALUE = 1;
    private static int CHANGE_DIST_VALUE = 10; // original value from Paper: 10.0
    private static int UNIFORM_DIST_VALUE = 50;

    public EADefaultFeedbackLoop(SimulationClient networkMgmt, IMoteWriter moteWriter) {
        super(networkMgmt, moteWriter);
    }

    @Override
    void planning() {

        // Go through all links
        boolean powerChanging = false;
        Link left, right;
        for (Mote mote : motes) {
            powerChanging = false;
            for (Link link : mote.getLinks()) {
                if (link.getSNR() > 0 && link.getPower() > 0) {
                    LOGGER.debug(
                            String.format("Link %02d: adapt link power: %d", getLinkNumber(link), -CHANGE_POWER_VALUE));
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - CHANGE_POWER_VALUE));
                    powerChanging = true;
                } else if (link.getSNR() < 0 && link.getPower() < 15) {
                    LOGGER.debug(
                            String.format("Link %02d: adapt link power: %d", getLinkNumber(link), +CHANGE_POWER_VALUE));
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() + CHANGE_POWER_VALUE));
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
                            LOGGER
                                .debug(String.format("Mote %02d: change distribution: left > right", mote.getMoteid()));
                            steps.add(new PlanningStep(Step.CHANGE_DIST, right,
                                    right.getDistribution() + CHANGE_DIST_VALUE));
                            steps.add(new PlanningStep(Step.CHANGE_DIST, left,
                                    left.getDistribution() - CHANGE_DIST_VALUE));
                        }
                    } else {
                        if (left.getDistribution() <= 100 - CHANGE_DIST_VALUE) {
                            LOGGER
                                .debug(String.format("Mote %02d: change distribution: right > left", mote.getMoteid()));
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
        return "EADefaultFeedbackLoop";
    }
}
