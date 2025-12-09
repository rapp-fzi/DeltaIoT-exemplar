package mapek.strategy;

import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import util.IMoteWriter;

class EAFeedbackLoopStrategy0d extends FeedbackLoop {

    private static int UNIFORM_DIST_VALUE = 50;

    private final StrategyConfigurationEAStrategy0d config;

    public EAFeedbackLoopStrategy0d(SimulationClient networkMgmt, IMoteWriter moteWriter,
            StrategyConfigurationEAStrategy0d configuration) {
        super(networkMgmt, moteWriter);
        this.config = configuration;
    }

    @Override
    protected boolean planDistribution(boolean powerChanging) {
        if (config.TIKTOK_ADAPTION) {
            if (powerChanging) {
                return false;
            }
        }
        return true;
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
                    int maxChange = Math.min(config.CHANGE_POWER_VALUE, link.getPower());
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - maxChange));
                    powerChanging = true;
                } else if (link.getSNR() < 0 && link.getPower() < 15) {
                    int maxChange = Math.min(config.CHANGE_POWER_VALUE, 15 - link.getPower());
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
                        if (right.getDistribution() <= 100 - config.CHANGE_DIST_VALUE) {
                            steps.add(new PlanningStep(Step.CHANGE_DIST, right,
                                    right.getDistribution() + config.CHANGE_DIST_VALUE));
                            steps.add(new PlanningStep(Step.CHANGE_DIST, left,
                                    left.getDistribution() - config.CHANGE_DIST_VALUE));
                        }
                    } else {
                        if (left.getDistribution() <= 100 - config.CHANGE_DIST_VALUE) {
                            steps.add(new PlanningStep(Step.CHANGE_DIST, left,
                                    left.getDistribution() + config.CHANGE_DIST_VALUE));
                            steps.add(new PlanningStep(Step.CHANGE_DIST, right,
                                    right.getDistribution() - config.CHANGE_DIST_VALUE));
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
        return "EAFeedbackLoopStrategy0d";
    }
}
