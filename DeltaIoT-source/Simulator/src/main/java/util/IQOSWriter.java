package util;

import java.io.IOException;
import java.util.ArrayList;

import simulator.QoS;

public interface IQOSWriter {
    void saveQoS(ArrayList<QoS> result, String strategyId) throws IOException;

}
