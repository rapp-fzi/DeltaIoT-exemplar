package deltaiot.console;

import java.util.ArrayList;
import java.util.List;

import mapek.strategy.IStrategyConfiguration;
import simulator.QoS;

public class Result {
    public final String strategy;
    public final IStrategyConfiguration strategyConfig;
    public final double energyConsumptionAverage;
    public final double packetLossAverage;
    public final double score;
    public final List<QoS> qos;

    public Result(String strategy, IStrategyConfiguration strategyConfig, double energyConsumptionAverage,
            double packetLossAverage, double score, List<QoS> qos) {
        this.strategy = strategy;
        this.strategyConfig = strategyConfig;
        this.energyConsumptionAverage = energyConsumptionAverage;
        this.packetLossAverage = packetLossAverage;
        this.score = score;
        this.qos = new ArrayList<>(qos);
    }

}
