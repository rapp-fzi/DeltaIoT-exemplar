package util;

import java.io.IOException;
import java.util.List;

import simulator.QoS;

public interface IQOSWriter {
    void saveQoS(List<QoS> result, String strategyId) throws IOException;

}
