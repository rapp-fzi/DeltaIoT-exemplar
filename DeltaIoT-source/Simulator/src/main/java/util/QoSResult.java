package util;

import java.util.Collections;
import java.util.List;

import simulator.QoS;

public class QoSResult {
    private final List<QoS> qosEntries;

    public QoSResult(List<QoS> qosEntries) {
        this.qosEntries = Collections.unmodifiableList(qosEntries);
    }

    public List<QoS> getQosEntries() {
        return qosEntries;
    }
}
