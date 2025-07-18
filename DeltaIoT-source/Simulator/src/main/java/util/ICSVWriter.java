package util;

import java.util.ArrayList;
import java.util.List;

import deltaiot.services.Mote;
import simulator.QoS;

public interface ICSVWriter {
    void saveQoS(ArrayList<QoS> result, String strategyId);

    void saveConfiguration(List<Mote> motes, int run, String strategyId);
}
