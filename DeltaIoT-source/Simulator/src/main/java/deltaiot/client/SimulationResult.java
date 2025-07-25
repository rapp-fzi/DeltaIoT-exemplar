package deltaiot.client;

import java.util.List;

import simulator.QoS;

public class SimulationResult implements ISimulationResult {
    private final List<QoS> qos;

    public SimulationResult(List<QoS> qos) {
        this.qos = qos;
    }

    @Override
    public List<QoS> getQoS() {
        return qos;
    }

}
