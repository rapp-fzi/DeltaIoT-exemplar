package mapek.strategy;

import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import util.IMoteWriter;

class EAFeedbackLoopStrategy1a extends FeedbackLoop {

    private final StrategyConfigurationEAStrategy1a config;

    public EAFeedbackLoopStrategy1a(SimulationClient networkMgmt, IMoteWriter moteWriter,
            StrategyConfigurationEAStrategy1a configuration) {
        super(networkMgmt, moteWriter);
        this.config = configuration;
    }

    @Override
    void planning() {

        // Go through all links
        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                int linkNumber = getLinkNumber(link);
                int changePowerValue = getChangePowerValue(linkNumber);

                if (link.getSNR() > 0 && link.getPower() > 0) {
                    int maxChange = Math.min(changePowerValue, link.getPower());
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - maxChange));
                } else if (link.getSNR() < 0 && link.getPower() < 15) {
                    int maxChange = Math.min(changePowerValue, 15 - link.getPower());
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() + maxChange));
                }
            }
        }

        if (steps.size() > 0) {
            execution();
        }
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
    public String getId() {
        return "EAFeedbackLoopStrategy1a";
    }
}
