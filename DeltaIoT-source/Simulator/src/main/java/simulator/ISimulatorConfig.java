package simulator;

import java.util.random.RandomGenerator;

public interface ISimulatorConfig {
    int getNumOfRuns();

    RandomGenerator getRandomGenerator();
}
