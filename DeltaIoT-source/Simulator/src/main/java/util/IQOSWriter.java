package util;

import java.io.IOException;

public interface IQOSWriter {
    void saveQoS(QoSResult qosResult, String strategyId) throws IOException;

}
