package mapek;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.client.Effector;
import deltaiot.client.Probe;
import deltaiot.services.Link;
import deltaiot.services.LinkSettings;
import deltaiot.services.Mote;
import util.IMoteWriter;

public class FeedbackLoop {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackLoop.class);

    private final int numOfRuns;
    private final Probe probe;
    private final Effector effector;
    private final IMoteWriter moteWriter;

    private int counter = -1;

    // Knowledge
    protected List<Mote> motes;
    protected List<PlanningStep> steps = new LinkedList<>();

    public FeedbackLoop(int numOfRuns, Probe probe, Effector effector, IMoteWriter moteWriter) {
        this.numOfRuns = numOfRuns;
        this.probe = probe;
        this.effector = effector;
        this.moteWriter = moteWriter;
    }

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
            for (Link link : mote.getLinks()) {
                LOGGER.info(link.toString());
            }
        }
        LOGGER.info("******** END *******");
    }

    void analysis() {

        // analyze all link settings
        boolean adaptationRequired = analyzeLinkSettings();

        // if adaptation required invoke the planner
        if (adaptationRequired) {
            planning();
        }
    }

    boolean analyzeLinkSettings() {
        // analyze all links for possible adaptation options
        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                if (link.getSNR() > 0 && link.getPower() > 0 || link.getSNR() < 0 && link.getPower() < 15) {
                    return true;
                }
            }
            if (mote.getLinks()
                .size() == 2) {
                if (mote.getLinks()
                    .get(0)
                    .getPower() != mote.getLinks()
                        .get(1)
                        .getPower())
                    return true;
            }
        }
        return false;
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
                .size() == 2 && powerChanging == false) {
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

    public String getId() {
        return "DefaultDeltaIoTStrategy";
    }
}
