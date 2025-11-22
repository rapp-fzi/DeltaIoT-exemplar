package mapek.strategy;

import deltaiot.client.SimulationClient;
import util.IMoteWriter;

public class AdaptionStrategyFactory {
    public enum Kind {
        Default(StrategyConfigurationDefault.class), //
        Quality(StrategyConfigurationDefault.class), //
        EADefault(StrategyConfigurationDefault.class), //
        EAStrategy1a(StrategyConfigurationEAStrategy1a.class), //
        EAStrategy1b(StrategyConfigurationEAStrategy1b.class), //
        EAStrategy1c(StrategyConfigurationEAStrategy1c.class), //
        EAStrategy2a(StrategyConfigurationEAStrategy2a.class), //
        EAStrategy2b(StrategyConfigurationEAStrategy2b.class), //
        EAStrategy3a(StrategyConfigurationEAStrategy3a.class), //
        EAStrategy3b(StrategyConfigurationEAStrategy3b.class), //
        EAStrategy3c(StrategyConfigurationEAStrategy3c.class), //
        ;

        private final Class<? extends IStrategyConfiguration> strategyConfiguration;

        private Kind(Class<? extends IStrategyConfiguration> strategyConfiguration) {
            this.strategyConfiguration = strategyConfiguration;
        }

        public Class<? extends IStrategyConfiguration> getStrategyConfiguration() {
            return strategyConfiguration;
        }
    }

    public IAdaptionStrategy create(Kind kind, SimulationClient networkMgmt, IMoteWriter moteWriter,
            IStrategyConfiguration config) {
        return switch (kind) {
        case Default -> new FeedbackLoop(networkMgmt, moteWriter);
        case Quality -> new QualityBasedFeedbackLoop(networkMgmt, moteWriter);
        case EADefault -> new EADefaultFeedbackLoop(networkMgmt, moteWriter);
        case EAStrategy1a -> new EAFeedbackLoopStrategy1a(networkMgmt, moteWriter,
                (StrategyConfigurationEAStrategy1a) config);
        case EAStrategy1b -> new EAFeedbackLoopStrategy1b(networkMgmt, moteWriter,
                (StrategyConfigurationEAStrategy1b) config);
        case EAStrategy1c -> new EAFeedbackLoopStrategy1c(networkMgmt, moteWriter,
                (StrategyConfigurationEAStrategy1c) config);
        case EAStrategy2a -> new EAFeedbackLoopStrategy2a(networkMgmt, moteWriter,
                (StrategyConfigurationEAStrategy2a) config);
        case EAStrategy2b -> new EAFeedbackLoopStrategy2b(networkMgmt, moteWriter,
                (StrategyConfigurationEAStrategy2b) config);
        case EAStrategy3a -> new EAFeedbackLoopStrategy3a(networkMgmt, moteWriter,
                (StrategyConfigurationEAStrategy3a) config);
        case EAStrategy3b -> new EAFeedbackLoopStrategy3b(networkMgmt, moteWriter,
                (StrategyConfigurationEAStrategy3b) config);
        case EAStrategy3c -> new EAFeedbackLoopStrategy3c(networkMgmt, moteWriter,
                (StrategyConfigurationEAStrategy3c) config);
        };
    }
}
