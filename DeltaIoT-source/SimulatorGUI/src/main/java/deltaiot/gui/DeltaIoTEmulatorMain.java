package deltaiot.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import deltaiot.gui.service.IDataDisplay;
import deltaiot.gui.service.ISimulatorProvider;
import deltaiot.gui.service.ServiceAdaption;
import deltaiot.gui.service.ServiceDisplayTopology;
import deltaiot.gui.service.ServiceEmulation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import simulator.IRunMonitor;
import simulator.QoS;
import simulator.Simulator;

public class DeltaIoTEmulatorMain extends Application implements ISimulatorProvider, IDataDisplay {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaIoTEmulatorMain.class);
    private static Path USER_PATH = Paths.get(System.getProperty("user.dir"));
    private static Path RESOURCE_PATH = USER_PATH.resolve("resources");

    @FXML
    private Button runEmulator, btnSaveResults, btnAdaptationLogic, btnClearResults, btnDisplay;

    @FXML
    private LineChart<Integer, Double> chartPacketLoss, chartEnergyConsumption;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label lblProgress;

    private List<String> data = new LinkedList<>();
    private Stage primaryStage;

    private ServiceEmulation serviceEmulation;
    private ServiceAdaption serviceAdaptation;

    @Override
    public Simulator getSimulator() {
        return null;
    }

    // ActivFORMSDeploy client;

    @FXML
    void runEmulatorClicked(ActionEvent event) {
        if (!serviceEmulation.isRunning()) {
            serviceEmulation.restart();
        }
    }

    @FXML
    void btnAdaptationLogic(ActionEvent event) {
        if (!serviceAdaptation.isRunning()) {
            serviceAdaptation.restart();
        }
    }

    @FXML
    void btnDisplay(ActionEvent event) {
        Service<Void> serviceDisplay = new ServiceDisplayTopology(this, RESOURCE_PATH);
        serviceDisplay.start();
    }

    @FXML
    void initialize() {
        assert progressBar != null : "fx:id=\"progressBar\" was not injected: check your FXML file 'Progress.fxml'.";

        IRunMonitor runMonitor = new IRunMonitor() {

            @Override
            public void onRun(int current, int max) {
                final double progress;
                if (max > 0) {
                    progress = (double) current / max;
                } else {
                    progress = 0.0;
                }
                Platform.runLater(() -> {
                    try {
                        progressBar.setProgress(progress);
                        lblProgress.setText("(" + current + "/" + max + ")");
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
            }
        };

        Path resultLocation = USER_PATH.resolve("results");
        serviceEmulation = new ServiceEmulation(this, runMonitor, resultLocation, btnDisplay);
        serviceAdaptation = new ServiceAdaption(this, runMonitor, resultLocation, btnDisplay);
    }

    @FXML
    void btnSaveResults(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters()
            .add(extFilter);

        // Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            saveFile(file);
        }
    }

    private void saveFile(File file) {
        try {
            FileWriter fileWriter = null;

            fileWriter = new FileWriter(file, true);

            for (String line : data)
                fileWriter.write(line + "\n");
            fileWriter.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @FXML
    void btnClearResults(ActionEvent event) {
        chartEnergyConsumption.getData()
            .clear();
        chartPacketLoss.getData()
            .clear();
        data.clear();
    }

    @Override
    public void displayData(List<QoS> qosList, String setName) {
        XYChart.Series<Integer, Double> energyConsumptionSeries = new XYChart.Series<>();
        XYChart.Series<Integer, Double> packetLossSeries = new XYChart.Series<>();
        energyConsumptionSeries.setName(setName);
        packetLossSeries.setName(setName);

        for (QoS qos : qosList) {
            data.add(qos + ", " + setName);
            energyConsumptionSeries.getData()
                .add(new XYChart.Data<>(qos.getPeriod(), qos.getEnergyConsumption()));
            packetLossSeries.getData()
                .add(new XYChart.Data<>(qos.getPeriod(), qos.getPacketLoss()));
        }
        chartEnergyConsumption.getData()
            .add(energyConsumptionSeries);
        chartPacketLoss.getData()
            .add(packetLossSeries);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Path guifxml = RESOURCE_PATH.resolve("EmulatorGUI.fxml");
        URL url = guifxml.toUri()
            .toURL();
        Parent root = FXMLLoader.load(url);
        primaryStage = stage;
        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("DeltaIoT");
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent arg0) {
                Platform.exit();
            }

        });
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
