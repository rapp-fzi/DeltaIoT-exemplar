package util;

import java.util.List;

import deltaiot.services.Mote;

public interface IMoteWriter {
    void saveConfiguration(List<Mote> motes, int run, String strategyId);
}
