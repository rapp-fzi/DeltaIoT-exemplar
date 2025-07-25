package simulator;

public class SimulatorConfig implements ISimulatorConfig {
    private final int numOfRuns;

    public SimulatorConfig(int numOfRuns) {
        this.numOfRuns = numOfRuns;
    }

    @Override
    public int getNumOfRuns() {
        return numOfRuns;
    }
}
