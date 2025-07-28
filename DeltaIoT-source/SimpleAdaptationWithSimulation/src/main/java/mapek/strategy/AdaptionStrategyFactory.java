package mapek.strategy;

import deltaiot.client.SimulationClient;
import util.IMoteWriter;

public class AdaptionStrategyFactory {
    public enum Kind {
        Default(DefaultStrategyConfiguration.class), //
        Quality(DefaultStrategyConfiguration.class), //
        EADefault(DefaultStrategyConfiguration.class), //
        EAStrategy1a(DefaultStrategyConfiguration.class), //
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
        case EAStrategy1a -> new EAFeedbackLoopStrategy1a(networkMgmt, moteWriter, (DefaultStrategyConfiguration) config);
        };
    }
}
