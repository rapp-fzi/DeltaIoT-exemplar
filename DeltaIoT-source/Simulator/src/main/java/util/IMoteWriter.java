package util;

import java.io.IOException;
import java.util.List;

import deltaiot.services.Mote;

public interface IMoteWriter {
    void saveConfiguration(List<Mote> motes, int run, String strategyId) throws IOException;
}
