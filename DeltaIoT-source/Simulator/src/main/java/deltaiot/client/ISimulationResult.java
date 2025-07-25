package deltaiot.client;

import java.util.List;

import simulator.QoS;

public interface ISimulationResult {
    String getStrategyId();

    List<QoS> getQoS();
}
