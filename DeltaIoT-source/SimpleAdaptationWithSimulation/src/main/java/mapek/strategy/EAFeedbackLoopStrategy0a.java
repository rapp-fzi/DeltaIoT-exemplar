package mapek.strategy;

import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import util.IMoteWriter;

class EAFeedbackLoopStrategy0a extends FeedbackLoop {

    private static final int CHANGE_DIST_VALUE = 10;
    private static final int UNIFORM_DIST_VALUE = 50;
    private static final int DIST_MIN = 0;
    private static final int DIST_MIN_MAX_DELTA = 100;
    private static final int DIST_UPPER = DIST_MIN + DIST_MIN_MAX_DELTA;
    private static final int DIST_MAX = DIST_UPPER - CHANGE_DIST_VALUE + 1;

    private final StrategyConfigurationEAStrategy0a config;

    public EAFeedbackLoopStrategy0a(SimulationClient networkMgmt, IMoteWriter moteWriter,
            StrategyConfigurationEAStrategy0a configuration) {
        super(networkMgmt, moteWriter);
        this.config = configuration;
    }

    @Override
    void planning() {

        // Go through all links
        boolean powerChanging = false;
        Link left, right;
        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                powerChanging = false;
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
                .size() == 2 && powerChanging == false) {
                left = mote.getLinks()
                    .get(0);
                right = mote.getLinks()
                    .get(1);
                if (left.getPower() != right.getPower()) {
                    // If distribution of all links is 100 then change it to 50
                    // 50
                    if (left.getDistribution() == DIST_UPPER && right.getDistribution() == DIST_UPPER) {
                        left.setDistribution(UNIFORM_DIST_VALUE);
                        right.setDistribution(UNIFORM_DIST_VALUE);
                    }
                    if (left.getPower() > right.getPower() && left.getDistribution() < DIST_MAX) {
                        steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() + CHANGE_DIST_VALUE));
                        steps.add(
                                new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() - CHANGE_DIST_VALUE));
                    } else if (right.getDistribution() < DIST_MAX) {
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
        return "DeltaIoTEAStrategy0aReconfigurationStrategy";
    }
}
