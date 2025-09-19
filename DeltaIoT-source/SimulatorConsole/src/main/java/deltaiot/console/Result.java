package deltaiot.console;

import mapek.strategy.IStrategyConfiguration;

public class Result {
    public final String strategy;
    public final IStrategyConfiguration strategyConfig;
    public final double energyConsumptionAverage;
    public final double packetLossAverage;
    public final double score;

    public Result(String strategy, IStrategyConfiguration strategyConfig, double energyConsumptionAverage,
            double packetLossAverage, double score) {
        this.strategy = strategy;
        this.strategyConfig = strategyConfig;
        this.energyConsumptionAverage = energyConsumptionAverage;
        this.packetLossAverage = packetLossAverage;
        this.score = score;
    }

}
