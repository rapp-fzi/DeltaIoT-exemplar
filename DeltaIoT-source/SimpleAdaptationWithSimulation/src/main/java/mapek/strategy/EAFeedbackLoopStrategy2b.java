package mapek.strategy;

import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import util.IMoteWriter;

class EAFeedbackLoopStrategy2b extends FeedbackLoop {

    // private static final int CHANGE_DIST_VALUE = 10;
    private static final int UNIFORM_DIST_VALUE = 50;
    private static final int DIST_MIN = 0;
    private static final int DIST_MIN_MAX_DELTA = 100;
    private static final int DIST_UPPER = DIST_MIN + DIST_MIN_MAX_DELTA;
    // private static final int DIST_MAX = DIST_UPPER - CHANGE_DIST_VALUE + 1;

    // will be assigned later down
    private int DIST_MAX = -1;
    private int[] POWER_LOWER = new int[17];

    // private int POWER_UPPER = -1;
    // private int POWER_MAX = -1;
    private int[] POWER_MAX = new int[17];
    private int[] POWER_UPPER = new int[17];

    private final StrategyConfigurationEAStrategy2b config;

    public EAFeedbackLoopStrategy2b(SimulationClient networkMgmt, IMoteWriter moteWriter,
            StrategyConfigurationEAStrategy2b configuration) {
        super(networkMgmt, moteWriter);
        this.config = configuration;
    }

    protected int getChangePowerValue(int linkNumber) {
        switch (linkNumber) {
        case 1:
            return config.CHANGE_POWER_VALUE1;
        case 2:
            return config.CHANGE_POWER_VALUE2;
        case 3:
            return config.CHANGE_POWER_VALUE3;
        case 4:
            return config.CHANGE_POWER_VALUE4;
        case 5:
            return config.CHANGE_POWER_VALUE5;
        case 6:
            return config.CHANGE_POWER_VALUE6;
        case 7:
            return config.CHANGE_POWER_VALUE7;
        case 8:
            return config.CHANGE_POWER_VALUE8;
        case 9:
            return config.CHANGE_POWER_VALUE9;
        case 10:
            return config.CHANGE_POWER_VALUE10;
        case 11:
            return config.CHANGE_POWER_VALUE11;
        case 12:
            return config.CHANGE_POWER_VALUE12;
        case 13:
            return config.CHANGE_POWER_VALUE13;
        case 14:
            return config.CHANGE_POWER_VALUE14;
        case 15:
            return config.CHANGE_POWER_VALUE15;
        case 16:
            return config.CHANGE_POWER_VALUE16;
        case 17:
            return config.CHANGE_POWER_VALUE17;
        }
        throw new IndexOutOfBoundsException(String.format("invalid link number %d (valid: 1-17)", linkNumber));
    }

    @Override
    protected void initRun() {
        DIST_MAX = DIST_UPPER - config.CHANGE_DIST_VALUE + 1;

        // POWER_LOWER = config.POWER_MIN + config.CHANGE_POWER_VALUE - 1;
        POWER_LOWER[0] = config.POWER_MIN + config.CHANGE_POWER_VALUE1 - 1;
        POWER_LOWER[1] = config.POWER_MIN + config.CHANGE_POWER_VALUE2 - 1;
        POWER_LOWER[2] = config.POWER_MIN + config.CHANGE_POWER_VALUE3 - 1;
        POWER_LOWER[3] = config.POWER_MIN + config.CHANGE_POWER_VALUE4 - 1;
        POWER_LOWER[4] = config.POWER_MIN + config.CHANGE_POWER_VALUE5 - 1;
        POWER_LOWER[5] = config.POWER_MIN + config.CHANGE_POWER_VALUE6 - 1;
        POWER_LOWER[6] = config.POWER_MIN + config.CHANGE_POWER_VALUE7 - 1;
        POWER_LOWER[7] = config.POWER_MIN + config.CHANGE_POWER_VALUE8 - 1;
        POWER_LOWER[8] = config.POWER_MIN + config.CHANGE_POWER_VALUE9 - 1;
        POWER_LOWER[9] = config.POWER_MIN + config.CHANGE_POWER_VALUE10 - 1;
        POWER_LOWER[10] = config.POWER_MIN + config.CHANGE_POWER_VALUE11 - 1;
        POWER_LOWER[11] = config.POWER_MIN + config.CHANGE_POWER_VALUE12 - 1;
        POWER_LOWER[12] = config.POWER_MIN + config.CHANGE_POWER_VALUE13 - 1;
        POWER_LOWER[13] = config.POWER_MIN + config.CHANGE_POWER_VALUE14 - 1;
        POWER_LOWER[14] = config.POWER_MIN + config.CHANGE_POWER_VALUE15 - 1;
        POWER_LOWER[15] = config.POWER_MIN + config.CHANGE_POWER_VALUE16 - 1;
        POWER_LOWER[16] = config.POWER_MIN + config.CHANGE_POWER_VALUE17 - 1;

        // POWER_MAX = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[0] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[1] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[2] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[3] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[4] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[5] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[6] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[7] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[8] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[9] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[10] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[11] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[12] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[13] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[14] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[15] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_MAX[16] = config.POWER_MIN + config.POWER_MIN_MAX_DELTA;
        POWER_UPPER[0] = POWER_MAX[0] - config.CHANGE_POWER_VALUE1 + 1;
        POWER_UPPER[1] = POWER_MAX[1] - config.CHANGE_POWER_VALUE2 + 1;
        POWER_UPPER[2] = POWER_MAX[2] - config.CHANGE_POWER_VALUE3 + 1;
        POWER_UPPER[3] = POWER_MAX[3] - config.CHANGE_POWER_VALUE4 + 1;
        POWER_UPPER[4] = POWER_MAX[4] - config.CHANGE_POWER_VALUE5 + 1;
        POWER_UPPER[5] = POWER_MAX[5] - config.CHANGE_POWER_VALUE6 + 1;
        POWER_UPPER[6] = POWER_MAX[6] - config.CHANGE_POWER_VALUE7 + 1;
        POWER_UPPER[7] = POWER_MAX[7] - config.CHANGE_POWER_VALUE8 + 1;
        POWER_UPPER[8] = POWER_MAX[8] - config.CHANGE_POWER_VALUE9 + 1;
        POWER_UPPER[9] = POWER_MAX[9] - config.CHANGE_POWER_VALUE10 + 1;
        POWER_UPPER[10] = POWER_MAX[10] - config.CHANGE_POWER_VALUE11 + 1;
        POWER_UPPER[11] = POWER_MAX[11] - config.CHANGE_POWER_VALUE12 + 1;
        POWER_UPPER[12] = POWER_MAX[12] - config.CHANGE_POWER_VALUE13 + 1;
        POWER_UPPER[13] = POWER_MAX[13] - config.CHANGE_POWER_VALUE14 + 1;
        POWER_UPPER[14] = POWER_MAX[14] - config.CHANGE_POWER_VALUE15 + 1;
        POWER_UPPER[15] = POWER_MAX[15] - config.CHANGE_POWER_VALUE16 + 1;
        POWER_UPPER[16] = POWER_MAX[16] - config.CHANGE_POWER_VALUE17 + 1;
    }

    @Override
    protected boolean adaptationRequiredPower(Link link) {
        int linkNumber = getLinkNumber(link);
        int powerUpper = POWER_UPPER[linkNumber - 1];
        if (link.getSNR() > 0 && link.getPower() > config.POWER_MIN
                || link.getSNR() < 0 && link.getPower() < powerUpper) {
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
                int linkNumber = getLinkNumber(link);
                int powerLower = POWER_LOWER[linkNumber - 1];
                int powerUpper = POWER_UPPER[linkNumber - 1];
                int changePowerValue = getChangePowerValue(linkNumber);
                if (link.getSNR() > 0 && link.getPower() > powerLower) {
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - changePowerValue));
                    powerChanging = true;
                } else if (link.getSNR() < 0 && link.getPower() < powerUpper) {
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() + changePowerValue));
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
                        steps.add(new PlanningStep(Step.CHANGE_DIST, left,
                                left.getDistribution() + config.CHANGE_DIST_VALUE));
                        steps.add(new PlanningStep(Step.CHANGE_DIST, right,
                                right.getDistribution() - config.CHANGE_DIST_VALUE));
                    } else if (right.getDistribution() < DIST_MAX) {
                        steps.add(new PlanningStep(Step.CHANGE_DIST, right,
                                right.getDistribution() + config.CHANGE_DIST_VALUE));
                        steps.add(new PlanningStep(Step.CHANGE_DIST, left,
                                left.getDistribution() - config.CHANGE_DIST_VALUE));
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
