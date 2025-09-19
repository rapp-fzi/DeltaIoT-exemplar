package mapek.strategy;

import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import util.IMoteWriter;

public class EAFeedbackLoopStrategy1c extends FeedbackLoop {

    // private static final int CHANGE_DIST_VALUE = 10;
    private static final int UNIFORM_DIST_VALUE = 50;
    private static final int DIST_MIN = 0;
    private static final int DIST_MIN_MAX_DELTA = 100;
    private static final int DIST_UPPER = DIST_MIN + DIST_MIN_MAX_DELTA;
    // private static final int DIST_MAX = DIST_UPPER - CHANGE_DIST_VALUE + 1;

    // will be assigned later down
    private int POWER_UPPER = -1;
    private int POWER_MAX = -1;
    private int DIST_MAX_7_8 = -1;
    private int DIST_MAX_15_16 = -1;
    private int DIST_MAX_5_6 = -1;

    private final StrategyConfigurationEAStrategy1c config;

    public EAFeedbackLoopStrategy1c(SimulationClient networkMgmt, IMoteWriter moteWriter,
            StrategyConfigurationEAStrategy1c configuration) {
        super(networkMgmt, moteWriter);
        this.config = configuration;
    }

    @Override
    protected void initRun() {
        DIST_MAX_7_8 = DIST_UPPER - config.CHANGE_DIST_VALUE_7_8 + 1;
        DIST_MAX_15_16 = DIST_UPPER - config.CHANGE_DIST_VALUE_15_16 + 1;
        DIST_MAX_5_6 = DIST_UPPER - config.CHANGE_DIST_VALUE_5_6 + 1;

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

    private int getDistMax(Link left, Link right) {
        // var int DIST_MAX_7_8 = -1;
        if ((left.getDest() == 2) && (right.getDest() == 3)) {
            return DIST_MAX_7_8;
        }

        // var int DIST_MAX_15_16 = -1;
        if ((left.getDest() == 6) && (right.getDest() == 5)) {
            return DIST_MAX_15_16;
        }

        // var int DIST_MAX_5_6 = -1;
        if ((left.getDest() == 7) && (right.getDest() == 3)) {
            return DIST_MAX_5_6;
        }

        throw new RuntimeException(String.format("unknown Link left: %d->%d right: %d->%d", left.getSource(),
                left.getDest(), right.getSource(), right.getDest()));
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
                    int distMax = getDistMax(left, right);
                    int changeDistValue = getChangeDistValue(left, right);
                    if (left.getPower() > right.getPower() && left.getDistribution() < distMax) {
                        steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() + changeDistValue));
                        steps.add(new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() - changeDistValue));
                    } else if (right.getDistribution() < distMax) {
                        steps.add(new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() + changeDistValue));
                        steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() - changeDistValue));
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
        return "DeltaIoTEAStrategy1cReconfigurationStrategy";
    }
}
