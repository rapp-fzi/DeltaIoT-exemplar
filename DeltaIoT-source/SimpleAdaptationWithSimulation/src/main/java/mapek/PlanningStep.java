package mapek;

import deltaiot.services.Link;

public class PlanningStep {
    private final Step step;
    private final Link link;
    private final int value;

    public PlanningStep(Step step, Link link, int value) {
        this.step = step;
        this.link = link;
        this.value = value;
    }

    public Step getStep() {
        return step;
    }

    public Link getLink() {
        return link;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %d", step, link, value);
    }
}
