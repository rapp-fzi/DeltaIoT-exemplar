package mapek.strategy;

public class StrategyConfigurationEAStrategy1a extends StrategyConfigurationDefault {
    int CHANGE_POWER_VALUE; // optimizable int[1,4,1] closed (stop included)
    int POWER_MIN; // optimizable int[0,5,1] closed (stop included)
    int POWER_MIN_MAX_DELTA; // optimizable int[4,10,1] closed (stop included)
}
