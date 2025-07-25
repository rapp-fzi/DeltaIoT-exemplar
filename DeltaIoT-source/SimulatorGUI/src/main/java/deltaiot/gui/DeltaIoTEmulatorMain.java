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

import deltaiot.DeltaIoTSimulator;
import deltaiot.client.ISimulationResult;
import deltaiot.client.ISimulationRunner;
import deltaiot.client.SimpleRunner;
import deltaiot.client.SimulationClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
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
import main.SimpleAdaptation;
import mapek.strategy.AdaptionStrategyFactory;
import mapek.strategy.IAdaptionStrategy;
import simulator.QoS;
import simulator.QoSCalculator;
import simulator.Simulator;
import simulator.SimulatorConfig;
import simulator.SimulatorFactory;
import util.CsvFileWriter;
import util.ICSVWriter;
import util.IMoteWriter;
import util.IQOSWriter;

public class DeltaIoTEmulatorMain extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeltaIoTEmulatorMain.class);

    @FXML
    private Button runEmulator, btnSaveResults, btnAdaptationLogic, btnClearResults, btnDisplay;

    @FXML
    private LineChart<Integer, Double> chartPacketLoss, chartEnergyConsumption;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label lblProgress;

    public static final Path filePath = Paths.get(System.getProperty("user.dir") + "/data/data.csv");
    List<String> data = new LinkedList<>();
    Stage primaryStage;
    Simulator simul;
    Service<Void> serviceEmulation = new Service<>() {

        @Override
        protected void succeeded() {
            displayData("Without Adaptation", 0);
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    btnDisplay.setDisable(true);
                    try {
                        SimulatorConfig config = createConfig();
                        simul = SimulatorFactory.createExperimentSimulator(config);
                        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
                        ICSVWriter csvWriter = new CsvFileWriter(baseLocation);
                        ISimulationRunner runner = runNoAdaption(simul);
                        executeRunner(runner, csvWriter);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }

                    btnDisplay.setDisable(false);
                    return null;
                }
            };
        }
    };

    @FXML
    void runEmulatorClicked(ActionEvent event) {
        if (!serviceEmulation.isRunning()) {
            serviceEmulation.restart();
            serviceProgress.restart();
        }
    }

    Service<Void> serviceProgress = new Service<>() {
        @Override
        protected void succeeded() {

        }

        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
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
    };

    Service<Void> serviceAdaptation = new Service<>() {

        @Override
        protected void succeeded() {
            displayData("With Adaptation", 1);
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    btnDisplay.setDisable(true);
                    try {
                        SimulatorConfig config = createConfig();
                        simul = SimulatorFactory.createExperimentSimulator(config);
                        Path baseLocation = Paths.get(System.getProperty("user.dir"), "results");
                        ICSVWriter csvWriter = new CsvFileWriter(baseLocation);
                        ISimulationRunner runner = runWithAdaption(simul, csvWriter);
                        executeRunner(runner, csvWriter);
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    btnDisplay.setDisable(false);
                    return null;
                }
            };
        }
    };

    private SimulatorConfig createConfig() {
        SimulatorConfig config = new SimulatorConfig(DeltaIoTSimulator.NUM_OF_RUNS);
        return config;
    }

    private ISimulationRunner runNoAdaption(Simulator simulator) throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        SimpleRunner simpleRunner = new SimpleRunner(simulationClient);
        return simpleRunner;
    }

    private ISimulationRunner runWithAdaption(Simulator simulator, IMoteWriter moteWriter) throws IOException {
        SimulationClient simulationClient = new SimulationClient(simulator);
        // Create Feedback loop
        AdaptionStrategyFactory adaptionStrategyFactory = new AdaptionStrategyFactory();
        // FeedbackLoop feedbackLoop = new QualityBasedFeedbackLoop(networkMgmt);
        IAdaptionStrategy feedbackLoop = adaptionStrategyFactory.create(simulationClient, moteWriter);
        SimpleAdaptation adaption = new SimpleAdaptation(simulationClient, feedbackLoop);
        return adaption;
    }

    private void executeRunner(ISimulationRunner runner, IQOSWriter qosWriter) throws IOException {
        ISimulationResult result = runner.run();
        List<QoS> qos = result.getQoS();
        qosWriter.saveQoS(qos, result.getStrategyId());

        QoSCalculator qoSCalculator = new QoSCalculator();
        double energyConsumptionAverage = qoSCalculator.calcEnergyConsumptionAverage(qos);
        double packetLossAverage = qoSCalculator.calcPacketLossAverage(qos);
        double score = qoSCalculator.calcScore(qos);
        LOGGER.info("result average energy {}, packet loss {}", energyConsumptionAverage, packetLossAverage);
        LOGGER.info("result score: {}", score);
    }

    // ActivFORMSDeploy client;

    @FXML
    void btnAdaptationLogic(ActionEvent event) {
        if (!serviceAdaptation.isRunning()) {
            serviceAdaptation.restart();
            serviceProgress.restart();
        }
    }

    @FXML
    void btnDisplay(ActionEvent event) {
        try {
            Path userPath = Paths.get(System.getProperty("user.dir"));
            Path guifxml = userPath.resolve("resources")
                .resolve("DeltaIoTModel.fxml");
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
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        assert progressBar != null : "fx:id=\"progressBar\" was not injected: check your FXML file 'Progress.fxml'.";
        lblProgress.textProperty()
            .bind(serviceProgress.messageProperty());
        progressBar.progressProperty()
            .bind(serviceProgress.progressProperty());
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
        } catch (IOException ex) {
            ex.printStackTrace();
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

    void displayData(String setName, int index) {
        XYChart.Series<Integer, Double> energyConsumptionSeries = new XYChart.Series<>();
        XYChart.Series<Integer, Double> packetLossSeries = new XYChart.Series<>();
        energyConsumptionSeries.setName(setName);
        packetLossSeries.setName(setName);
        List<QoS> qosList = simul.getQosValues();

        for (QoS qos : qosList) {
            data.add(qos + ", " + setName);
            energyConsumptionSeries.getData()
                .add(new XYChart.Data<>(Integer.parseInt(qos.getPeriod()), qos.getEnergyConsumption()));
            packetLossSeries.getData()
                .add(new XYChart.Data<>(Integer.parseInt(qos.getPeriod()), qos.getPacketLoss()));
        }
        chartEnergyConsumption.getData()
            .add(energyConsumptionSeries);
        chartPacketLoss.getData()
            .add(packetLossSeries);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Path userPath = Paths.get(System.getProperty("user.dir"));
        Path guifxml = userPath.resolve("resources")
            .resolve("EmulatorGUI.fxml");
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
