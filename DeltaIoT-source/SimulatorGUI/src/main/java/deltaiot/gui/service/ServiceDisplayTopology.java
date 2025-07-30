package deltaiot.gui.service;

import java.net.URL;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.gui.DeltaIoTClientMain;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import simulator.Simulator;

public class ServiceDisplayTopology extends Service<Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDisplayTopology.class);

    private final ISimulatorProvider simulatorProvider;
    private final Path resourcePath;

    public ServiceDisplayTopology(ISimulatorProvider simulatorProvider, Path resourcePath) {
        this.simulatorProvider = simulatorProvider;
        this.resourcePath = resourcePath;
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
                Platform.runLater(() -> {
                    displayStage(simul);
                });

                return null;
            }

            private void displayStage(Simulator simul) {
                try {
                    Path guifxml = resourcePath.resolve("DeltaIoTModel.fxml");
                    URL url = guifxml.toUri()
                        .toURL();
                    FXMLLoader fxmlLoader = new FXMLLoader(url);
                    Parent root1 = (Parent) fxmlLoader.load();
                    DeltaIoTClientMain main = fxmlLoader.getController();

                    main.setSimulationClient(simul);
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root1));
                    stage.setResizable(false);
                    stage.setTitle("DeltaIoT Topology");
                    stage.setAlwaysOnTop(true);
                    stage.showAndWait();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        };
    }
}
