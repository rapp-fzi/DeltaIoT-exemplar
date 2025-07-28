package mapek.strategy;

import deltaiot.client.SimulationClient;
import util.IMoteWriter;

public class AdaptionStrategyFactory {
    public enum Kind {
        Default, EADefault, Quality,
    }

    public IAdaptionStrategy create(Kind kind, SimulationClient networkMgmt, IMoteWriter moteWriter,
            IStrategyConfiguration config) {
        return switch (kind) {
        case Default -> new FeedbackLoop(networkMgmt, moteWriter);
        case EADefault -> new EADefaultFeedbackLoop(networkMgmt, moteWriter);
        case Quality -> new QualityBasedFeedbackLoop(networkMgmt, moteWriter);
        };
    }
}
