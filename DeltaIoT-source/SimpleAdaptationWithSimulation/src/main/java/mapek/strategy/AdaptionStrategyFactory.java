package mapek.strategy;

import deltaiot.client.SimulationClient;
import util.IMoteWriter;

public class AdaptionStrategyFactory {
    public IAdaptionStrategy create(SimulationClient networkMgmt, IMoteWriter moteWriter) {
        return new FeedbackLoop(networkMgmt, moteWriter);
    }
}
