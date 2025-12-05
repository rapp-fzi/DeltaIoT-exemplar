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

    // will be assigned later down
    private int POWER_LOWER = -1;
    private int POWER_UPPER = -1;
    private static final int POWER_MIN = 0;
    private static final int POWER_MAX = 15;

    private final StrategyConfigurationEAStrategy0a config;

    public EAFeedbackLoopStrategy0a(SimulationClient networkMgmt, IMoteWriter moteWriter,
            StrategyConfigurationEAStrategy0a configuration) {
        super(networkMgmt, moteWriter);
        this.config = configuration;
    }

    @Override
    protected void initRun() {
        POWER_LOWER = POWER_MIN + config.CHANGE_POWER_VALUE - 1;
        POWER_UPPER = POWER_MAX - config.CHANGE_POWER_VALUE + 1;
    }

    @Override
    protected boolean adaptationRequiredPower(Link link) {
        if (link.getSNR() > 0 && link.getPower() > POWER_MIN) {
            return true;
        }
        if (link.getSNR() < 0 && link.getPower() < POWER_UPPER) {
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
                if (link.getSNR() > 0 && link.getPower() > POWER_LOWER) {
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - config.CHANGE_POWER_VALUE));
                    powerChanging = true;
                } else if (link.getSNR() < 0 && link.getPower() < POWER_UPPER) {
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
