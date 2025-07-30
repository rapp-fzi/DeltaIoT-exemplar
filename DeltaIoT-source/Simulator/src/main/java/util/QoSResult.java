package util;

import java.util.Collections;
import java.util.List;

import simulator.QoS;

public class QoSResult {
    private final String strategyName;
    private final List<QoS> qosEntries;

    public QoSResult(String strategyName, List<QoS> qosEntries) {
        this.strategyName = strategyName;
        this.qosEntries = Collections.unmodifiableList(qosEntries);
    }

    public String getStrategyName() {
        return strategyName;
    }

    public List<QoS> getQosEntries() {
        return qosEntries;
    }
}
