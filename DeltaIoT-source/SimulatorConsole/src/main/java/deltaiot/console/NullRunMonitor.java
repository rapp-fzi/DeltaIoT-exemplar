package deltaiot.console;

import simulator.IRunMonitor;

public class NullRunMonitor implements IRunMonitor {

    @Override
    public void onRun(int current, int max) {
    }
}
