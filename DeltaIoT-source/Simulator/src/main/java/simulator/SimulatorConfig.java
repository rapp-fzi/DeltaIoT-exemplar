package simulator;

import java.util.random.RandomGenerator;

public class SimulatorConfig implements ISimulatorConfig {
    private final int numOfRuns;
    private final RandomGenerator randomGenerator;

    public SimulatorConfig(int numOfRuns, RandomGenerator randomGenerator) {
        this.numOfRuns = numOfRuns;
        this.randomGenerator = randomGenerator;
    }

    @Override
    public int getNumOfRuns() {
        return numOfRuns;
    }

    @Override
    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }
}
