package deltaiot.client;

import java.io.IOException;
import java.util.List;

import simulator.QoS;

public interface ISimulationRunner {
    List<QoS> run() throws IOException;
}
