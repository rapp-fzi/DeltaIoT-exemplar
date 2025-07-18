package util;

import java.util.ArrayList;

import simulator.QoS;

public interface IQOSWriter {
    void saveQoS(ArrayList<QoS> result, String strategyId);

}
