package mapek.strategy;

import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import util.IMoteWriter;

class EAFeedbackLoopStrategy1a extends FeedbackLoop {

    private static final int CHANGE_DIST_VALUE = 1; // original value from Paper: 10.0
    private static final int UNIFORM_DIST_VALUE = 5;
    private static final int DIST_MIN = 0;
    private static final int DIST_MIN_MAX_DELTA = 10;
    private static final int DIST_UPPER = DIST_MIN + DIST_MIN_MAX_DELTA;
    private static final int DIST_MAX = DIST_UPPER - CHANGE_DIST_VALUE + 1;

    private int POWER_UPPER = -1;
    private int POWER_MAX = -1; // will be assigned later down

    private final StrategyConfigurationEAStrategy1a config;

    public EAFeedbackLoopStrategy1a(SimulationClient networkMgmt, IMoteWriter moteWriter,
            StrategyConfigurationEAStrategy1a configuration) {
        super(networkMgmt, moteWriter);
        this.config = configuration;
    }

    @Override
    protected void initRun() {
        POWER_UPPER = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX = POWER_UPPER - config.CHANGE_POWER_VALUE + 1;
    }

    @Override
    protected boolean adaptationRequiredPower(Link link) {
        if (link.getSNR() > 0 && link.getPower() > config.POWER_MIN
                || link.getSNR() < 0 && link.getPower() < POWER_MAX) {
            return true;
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
                if (link.getSNR() > 0 && link.getPower() > config.POWER_MIN) {
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - config.CHANGE_POWER_VALUE));
                    powerChanging = true;
                } else if (link.getSNR() < 0 && link.getPower() < POWER_MAX) {
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() + config.CHANGE_POWER_VALUE));
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
        return "DeltaIoTEAStrategy1aReconfigurationStrategy";
    }
}
