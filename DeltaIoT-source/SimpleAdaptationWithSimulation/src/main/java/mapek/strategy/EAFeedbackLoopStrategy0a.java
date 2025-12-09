package mapek.strategy;

import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import util.IMoteWriter;

class EAFeedbackLoopStrategy0a extends FeedbackLoop {

    private final StrategyConfigurationEAStrategy0a config;

    public EAFeedbackLoopStrategy0a(SimulationClient networkMgmt, IMoteWriter moteWriter,
            StrategyConfigurationEAStrategy0a configuration) {
        super(networkMgmt, moteWriter);
        this.config = configuration;
    }

    @Override
    void planning() {

        // Go through all links
        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                if (link.getSNR() > 0 && link.getPower() > 0) {
                    int maxChange = Math.min(config.CHANGE_POWER_VALUE, link.getPower());
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - maxChange));
                } else if (link.getSNR() < 0 && link.getPower() < 15) {
                    int maxChange = Math.min(config.CHANGE_POWER_VALUE, 15 - link.getPower());
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() + maxChange));
                }
            }
        }

        if (steps.size() > 0) {
            execution();
        }
    }

    @Override
    public String getId() {
        return "EAFeedbackLoopStrategy0a";
    }
}
