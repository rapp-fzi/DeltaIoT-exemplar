package deltaiot.gui;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
import util.QoSResult;

public class DeltaIoTEmulatorMain extends Application implements ISimulatorProvider, IDataDisplay {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaIoTEmulatorMain.class);
    private static Path USER_PATH = Paths.get(System.getProperty("user.dir"));
    private static Path RESOURCE_PATH = USER_PATH.resolve("resources");
    private static Path RESULT_LOCATION = USER_PATH.resolve("results");

    @FXML
    private Button runEmulator, btnSaveResults, btnAdaptationLogic, btnClearResults, btnDisplay;

    @FXML
    private LineChart<Integer, Double> chartPacketLoss, chartEnergyConsumption;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label lblProgress;

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

        serviceEmulation = new ServiceEmulation(this, runMonitor, RESULT_LOCATION, btnDisplay);
        serviceAdaptation = new ServiceAdaption(this, runMonitor, RESULT_LOCATION, btnDisplay);
    }

    @FXML
    void btnLoadResults(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(RESULT_LOCATION.toFile());

        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSon files (*.json)", "*.json");
        fileChooser.getExtensionFilters()
            .add(extFilter);

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                QoSResult qosResult = readQoSResult(file.toPath());
                LOGGER.info("loaded result file: {}", file);
                LOGGER.info("strategy: {}", qosResult.getStrategyName());
                LOGGER.info("result average energy {}, packet loss {}", qosResult.getEnergyConsumptionAverage(),
                        qosResult.getPacketLossAverage());
                LOGGER.info("result score: {}", qosResult.getScore());
                displayData(qosResult.getQosEntries(), qosResult.getStrategyName());
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private QoSResult readQoSResult(Path resultFile) throws IOException {
        LOGGER.debug("read QoS results from: {}", resultFile);
        Gson gson = new GsonBuilder().create();
        try (Reader reader = Files.newBufferedReader(resultFile, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, QoSResult.class);
        }
    }

    @FXML
    void btnClearResults(ActionEvent event) {
        chartEnergyConsumption.getData()
            .clear();
        chartPacketLoss.getData()
            .clear();
    }

    @Override
    public void displayData(List<QoS> qosList, String setName) {
        XYChart.Series<Integer, Double> energyConsumptionSeries = new XYChart.Series<>();
        XYChart.Series<Integer, Double> packetLossSeries = new XYChart.Series<>();
        energyConsumptionSeries.setName(setName);
        packetLossSeries.setName(setName);

        for (QoS qos : qosList) {
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
