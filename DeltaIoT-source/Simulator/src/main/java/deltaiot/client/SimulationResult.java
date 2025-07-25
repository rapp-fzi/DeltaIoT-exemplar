package deltaiot.client;

import java.util.List;

import simulator.QoS;

public class SimulationResult implements ISimulationResult {
    private final String strategyId;
    private final List<QoS> qos;

    public SimulationResult(String strategyId, List<QoS> qos) {
        this.strategyId = strategyId;
        this.qos = qos;
    }

    @Override
    public String getStrategyId() {
        return strategyId;
    }

    @Override
    public List<QoS> getQoS() {
        return qos;
    }

}
