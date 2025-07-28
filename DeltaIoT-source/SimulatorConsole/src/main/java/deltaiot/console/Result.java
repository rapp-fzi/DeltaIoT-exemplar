package deltaiot.console;

public class Result {
    public final String strategy;
    public final double energyConsumptionAverage;
    public final double packetLossAverage;
    public final double score;

    public Result(String strategy, double energyConsumptionAverage, double packetLossAverage, double score) {
        this.strategy = strategy;
        this.energyConsumptionAverage = energyConsumptionAverage;
        this.packetLossAverage = packetLossAverage;
        this.score = score;
    }

}
