package deltaiot.console;

import java.util.ArrayList;
import java.util.List;

import mapek.strategy.IStrategyConfiguration;
import simulator.QoS;

public class Result {
    public final String strategy;
    public final IStrategyConfiguration strategyConfig;
    public final int num_runs;
    public final double energyConsumptionMin;
    public final double energyConsumptionMax;
    public final double energyConsumptionAverage;
    public final double packetLossMin;
    public final double packetLossMax;
    public final double packetLossAverage;
    public final double averageScore;
    public final List<QoS> qos;

    public Result(String strategy, IStrategyConfiguration strategyConfig, int num_runs, double energyConsumptionMin,
            double energyConsumptionMax, double energyConsumptionAverage, double packetLossMin, double packetLossMax,
            double packetLossAverage, double averageScore, List<QoS> qos) {
        this.strategy = strategy;
        this.strategyConfig = strategyConfig;
        this.num_runs = num_runs;
        this.energyConsumptionMin = energyConsumptionMin;
        this.energyConsumptionMax = energyConsumptionMax;
        this.energyConsumptionAverage = energyConsumptionAverage;
        this.packetLossMin = packetLossMin;
        this.packetLossMax = packetLossMax;
        this.packetLossAverage = packetLossAverage;
        this.averageScore = averageScore;
        this.qos = new ArrayList<>(qos);
    }

}
