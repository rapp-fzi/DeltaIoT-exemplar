package mapek.strategy;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.client.Effector;
import deltaiot.client.Probe;
import deltaiot.client.SimulationClient;
import deltaiot.services.Link;
import deltaiot.services.LinkSettings;
import deltaiot.services.Mote;
import mapek.PlanningStep;
import mapek.Step;
import util.IMoteWriter;

class FeedbackLoop implements IAdaptionStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackLoop.class);

    private final int numOfRuns;
    private final Probe probe;
    private final Effector effector;
    private final IMoteWriter moteWriter;

    private int counter = -1;

    // Knowledge
    protected List<Mote> motes;
    protected List<PlanningStep> steps = new LinkedList<>();

    public FeedbackLoop(SimulationClient networkMgmt, IMoteWriter moteWriter) {
        this.numOfRuns = networkMgmt.getSimulator()
            .getNumOfRuns();
        this.probe = networkMgmt.getProbe();
        this.effector = networkMgmt.getEffector();
        this.moteWriter = moteWriter;
    }

    @Override
    public void start() throws IOException {
        for (int i = 0; i < numOfRuns; i++) {
            initRun();
            monitor();
        }
    }

    protected void initRun() {
    }

    void monitor() throws IOException {
        motes = probe.getAllMotes();

        counter = (counter + 1) % numOfRuns;
        logConfiguration(motes, counter, getId());
        moteWriter.saveConfiguration(motes, counter, getId());

        // perform analysis
        analysis();
    }

    private void logConfiguration(List<Mote> motes, int run, String strategyId) {
        LOGGER.info("******** Network configuration of {} *******", run);
        for (Mote mote : motes) {
            LOGGER.info(String.format("Mote %02d", mote.getMoteid()));
            for (Link link : mote.getLinks()) {
                int linkNumber = getLinkNumber(link);
                LOGGER.info(String.format("  Link %02d: %s", linkNumber, link.toString()));
            }
        }
        LOGGER.info("******** END *******");
    }

    /**
     * 
     * @param link
     * @return link number in range 1-17
     */
    protected int getLinkNumber(Link link) {
        if ((link.getSource() == 13) && (link.getDest() == 11)) {
            return 1;
        }
        if ((link.getSource() == 14) && (link.getDest() == 12)) {
            return 2;
        }
        if ((link.getSource() == 15) && (link.getDest() == 12)) {
            return 3;
        }
        if ((link.getSource() == 11) && (link.getDest() == 7)) {
            return 4;
        }
        if ((link.getSource() == 12) && (link.getDest() == 7)) {
            return 5;
        }
        if ((link.getSource() == 12) && (link.getDest() == 3)) {
            return 6;
        }
        if ((link.getSource() == 7) && (link.getDest() == 3)) {
            return 7;
        }
        if ((link.getSource() == 7) && (link.getDest() == 2)) {
            return 8;
        }
        if ((link.getSource() == 2) && (link.getDest() == 4)) {
            return 9;
        }
        if ((link.getSource() == 3) && (link.getDest() == 1)) {
            return 10;
        }
        if ((link.getSource() == 8) && (link.getDest() == 1)) {
            return 11;
        }
        if ((link.getSource() == 4) && (link.getDest() == 1)) {
            return 12;
        }
        if ((link.getSource() == 9) && (link.getDest() == 1)) {
            return 13;
        }
        if ((link.getSource() == 6) && (link.getDest() == 4)) {
            return 14;
        }
        if ((link.getSource() == 10) && (link.getDest() == 6)) {
            return 15;
        }
        if ((link.getSource() == 10) && (link.getDest() == 5)) {
            return 16;
        }
        if ((link.getSource() == 5) && (link.getDest() == 9)) {
            return 17;
        }

        throw new RuntimeException(String.format("unknown link %d -> %d", link.getSource(), link.getDest()));
    }

    private void analysis() {

        // analyze all link settings
        boolean adaptationRequired = analyzeLinkSettings();

        // if adaptation required invoke the planner
        if (adaptationRequired) {
            planning();
        }
    }

    protected boolean isAdaptationRequired() {
        return analyzeLinkSettings();
    }

    private boolean analyzeLinkSettings() {
        // analyze all links for possible adaptation options
        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                if (adaptationRequiredPower(link)) {
                    return true;
                }
            }
            if (mote.getLinks()
                .size() == 2) {
                if (adaptationRequiredPowerDistribution(mote)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean adaptationRequiredPower(Link link) {
        if (link.getSNR() > 0 && link.getPower() > 0 || link.getSNR() < 0 && link.getPower() < 15) {
            return true;
        }
        return false;
    }

    protected boolean adaptationRequiredPowerDistribution(Mote mote) {
        if (mote.getLinks()
            .get(0)
            .getPower() != mote.getLinks()
                .get(1)
                .getPower()) {
            return true;
        }
        return false;
    }

    protected boolean planDistribution(boolean powerChanging) {
        if (powerChanging) {
            return false;
        }
        return true;
    }

    void planning() {

        // Go through all links
        boolean powerChanging = false;
        Link left, right;
        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                powerChanging = false;
                if (link.getSNR() > 0 && link.getPower() > 0) {
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - 1));
                    powerChanging = true;
                } else if (link.getSNR() < 0 && link.getPower() < 15) {
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() + 1));
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
                        left.setDistribution(50);
                        right.setDistribution(50);
                    }
                    if (left.getPower() > right.getPower() && left.getDistribution() < 100) {
                        steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() + 10));
                        steps.add(new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() - 10));
                    } else if (right.getDistribution() < 100) {
                        steps.add(new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() + 10));
                        steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() - 10));
                    }
                }
            }
        }

        if (steps.size() > 0) {
            execution();
        }
    }

    void execution() {
        boolean addMote;
        List<Mote> motesEffected = new LinkedList<>();
        for (Mote mote : motes) {
            addMote = false;
            for (PlanningStep step : steps) {
                if (step.getLink()
                    .getSource() == mote.getMoteid()) {
                    addMote = true;
                    if (step.getStep() == Step.CHANGE_POWER) {
                        mote.getLinkWithDest(step.getLink()
                            .getDest())
                            .setPower(step.getValue());
                    } else if (step.getStep() == Step.CHANGE_DIST) {
                        mote.getLinkWithDest(step.getLink()
                            .getDest())
                            .setDistribution(step.getValue());
                    }
                }
            }
            motesEffected.add(mote);
        }
        List<LinkSettings> newSettings;

        for (Mote mote : motesEffected) {
            newSettings = new LinkedList<>();
            for (Link link : mote.getLinks()) {
                newSettings.add(new LinkSettings(mote.getMoteid(), link.getDest(), link.getPower(),
                        link.getDistribution(), link.getSF()));
            }
            effector.setMoteSettings(mote.getMoteid(), newSettings);
        }
        steps.clear();
    }

    Mote findMote(int source, int destination) {
        for (Mote mote : motes) {
            if (mote.getMoteid() == source) {
                return mote;
            }
        }
        return null;
    }

    @Override
    public String getId() {
        return "FeedbackLoop";
    }
}
