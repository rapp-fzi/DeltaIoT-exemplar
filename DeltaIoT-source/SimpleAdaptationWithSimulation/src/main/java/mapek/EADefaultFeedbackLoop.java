package mapek;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import deltaiot.DeltaIoTSimulator;
import deltaiot.client.Effector;
import deltaiot.client.Probe;
import deltaiot.services.Link;
import deltaiot.services.LinkSettings;
import deltaiot.services.Mote;
import util.CsvFileWriter;

public class EADefaultFeedbackLoop extends FeedbackLoop {

    private static int CHANGE_POWER_VALUE = 1;
    private static int CHANGE_DIST_VALUE = 10; // original value from Paper: 10.0
    private static int UNIFORM_DIST_VALUE = 50;
    private static int TOTAL_DIST_VALUE = 100;

    Probe probe;
    Effector effector;

    int counter = -1;

    // Knowledge
    ArrayList<Mote> motes;
    List<PlanningStep> steps = new LinkedList<>();

    @Override
    public void setProbe(Probe probe) {
        this.probe = probe;
    }

    @Override
    public void setEffector(Effector effector) {
        this.effector = effector;
    }

    @Override
    public void start() {
        for (int i = 0; i < DeltaIoTSimulator.NUM_OF_RUNS; i++) {
            monitor();
        }
    }

    @Override
    void monitor() {
        motes = probe.getAllMotes();

        counter = (counter + 1) % DeltaIoTSimulator.NUM_OF_RUNS;
        CsvFileWriter.logAndSaveConfiguration(motes, counter, getId());

        // perform analysis
        analysis();
    }

    @Override
    void analysis() {

        // analyze all link settings
        boolean adaptationRequired = analyzeLinkSettings();

        // if adaptation required invoke the planner
        if (adaptationRequired) {
            planning();
        }
    }

    @Override
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

    @Override
    void planning() {

        // Go through all links
        boolean powerChanging = false;
        Link left, right;
        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                powerChanging = false;
                if (link.getSNR() > 0 && link.getPower() > 0) {
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() - CHANGE_POWER_VALUE));
                    powerChanging = true;
                } else if (link.getSNR() < 0 && link.getPower() < 15) {
                    steps.add(new PlanningStep(Step.CHANGE_POWER, link, link.getPower() + CHANGE_POWER_VALUE));
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
                    if (left.getDistribution() == TOTAL_DIST_VALUE && right.getDistribution() == TOTAL_DIST_VALUE) {
                        left.setDistribution(UNIFORM_DIST_VALUE);
                        right.setDistribution(UNIFORM_DIST_VALUE);
                    }
                    if (left.getPower() > right.getPower() && left.getDistribution() < TOTAL_DIST_VALUE) {
                        steps.add(new PlanningStep(Step.CHANGE_DIST, left, left.getDistribution() + CHANGE_DIST_VALUE));
                        steps.add(
                                new PlanningStep(Step.CHANGE_DIST, right, right.getDistribution() - CHANGE_DIST_VALUE));
                    } else if (right.getDistribution() < TOTAL_DIST_VALUE) {
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
    void execution() {
        boolean addMote;
        List<Mote> motesEffected = new LinkedList<>();
        for (Mote mote : motes) {
            addMote = false;
            for (PlanningStep step : steps) {
                if (step.link.getSource() == mote.getMoteid()) {
                    addMote = true;
                    if (step.step == Step.CHANGE_POWER) {
                        mote.getLinkWithDest(step.link.getDest())
                            .setPower(step.value);
                    } else if (step.step == Step.CHANGE_DIST) {
                        mote.getLinkWithDest(step.link.getDest())
                            .setDistribution(step.value);
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

    @Override
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
        return "DeltaIoTDefaultReconfigurationStrategy";
    }
}
