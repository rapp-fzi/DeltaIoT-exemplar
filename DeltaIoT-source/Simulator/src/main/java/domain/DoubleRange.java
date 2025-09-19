package domain;

import java.util.HashMap;
import java.util.Map;
import java.util.random.RandomGenerator;

public class DoubleRange implements Profile<Double> {

    private final RandomGenerator randomGenerator;

    private double min;
    private double max;
    private Map<Integer, Double> memory = new HashMap<>();

    public DoubleRange(RandomGenerator randomGenerator, Double min, Double max) {
        this.randomGenerator = randomGenerator;
        this.min = min;
        this.max = max;
    }

    @Override
    public Double get(int runNumber) {
        if (memory.containsKey(runNumber)) {
            return memory.get(runNumber);
        } else {
            double random = min + randomGenerator.nextDouble() * (max - min);
            memory.put(runNumber, random);
            return random;
        }
    }

}
