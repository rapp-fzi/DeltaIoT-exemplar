package deltaiot.client;

import java.util.List;

import simulator.QoS;

public interface ISimulationResult {
    List<QoS> getQoS();
}
