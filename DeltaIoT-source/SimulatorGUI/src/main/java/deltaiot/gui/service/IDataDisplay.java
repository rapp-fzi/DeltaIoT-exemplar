package deltaiot.gui.service;

import java.util.List;

import simulator.QoS;

public interface IDataDisplay {
    void displayData(List<QoS> qosList, String setName);
}
