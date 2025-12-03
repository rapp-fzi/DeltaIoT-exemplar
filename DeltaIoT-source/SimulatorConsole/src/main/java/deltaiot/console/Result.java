package deltaiot.console;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mapek.strategy.IStrategyConfiguration;
import simulator.QoS;

public class Result {
    public final String strategy;
    public final IStrategyConfiguration strategyConfig;
    public final int num_runs;
    public final Map<String, Map<String, Double>> statistics;
    public final double averageScore;
    public final double normalizedScore;
    public final List<QoS> qos;

    public Result(String strategy, IStrategyConfiguration strategyConfig, int num_runs, double energyConsumptionMin,
            double energyConsumptionMax, double energyConsumptionAverage, double packetLossMin, double packetLossMax,
            double packetLossAverage, double averageScore, double normalizedScore, List<QoS> qos) {
        this.strategy = strategy;
        this.strategyConfig = strategyConfig;
        this.num_runs = num_runs;
        this.statistics = new LinkedHashMap<>();
        Map<String, Double> energyStats = new LinkedHashMap<>();
        energyStats.put("min", energyConsumptionMin);
        energyStats.put("max", energyConsumptionMax);
        energyStats.put("average", energyConsumptionAverage);
        this.statistics.put("energyConsumption", energyStats);
        Map<String, Double> packetLossStats = new LinkedHashMap<>();
        packetLossStats.put("min", packetLossMin);
        packetLossStats.put("max", packetLossMax);
        packetLossStats.put("average", packetLossAverage);
        this.statistics.put("packetLoss", packetLossStats);
        this.averageScore = averageScore;
        this.normalizedScore = normalizedScore;
        this.qos = new ArrayList<>(qos);
    }

}
