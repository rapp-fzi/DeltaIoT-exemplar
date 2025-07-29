package deltaiot.gui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import simulator.Simulator;

public class ServiceProgress extends Service<Void> {
    private final ISimulatorProvider simulatorProvider;

    public ServiceProgress(ISimulatorProvider simulatorProvider) {
        this.simulatorProvider = simulatorProvider;
    }

    @Override
    protected void succeeded() {
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                Simulator simul = simulatorProvider.getSimulator();
                int run;
                do {
                    run = simul.getRunInfo()
                        .getRunNumber();

                    updateProgress(run, simul.getNumOfRuns());
                    updateMessage("(" + run + "/" + simul.getNumOfRuns() + ")");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } while (run < simul.getNumOfRuns());

                return null;
            }
        };
    }
}
