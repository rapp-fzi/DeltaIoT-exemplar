package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import deltaiot.services.Link;
import deltaiot.services.Mote;
import simulator.QoS;

public class CsvFileWriter implements ICSVWriter, IQOSWriter {
    public final static String CSV_DELIMITER = ";";

    @Override
    public void saveQoS(ArrayList<QoS> result, String strategyId) throws IOException {
        String csvFileName = strategyId + "Results.csv";
        String location = Paths.get(System.getProperty("user.dir"), "results", csvFileName)
            .toString();
        File csvOutputFile = new File(location);

        boolean csvFileExists = csvOutputFile.exists();

        List<String> csvRows = new ArrayList<>();
        if (csvFileExists == false) {
            csvOutputFile.createNewFile();

            String header = new StringBuilder().append("Time")
                .append(CSV_DELIMITER)
                .append("Packet loss")
                .append(CSV_DELIMITER)
                .append("Energy consumption")
                .toString();
            csvRows.add(header);
        }

        for (QoS each : result) {
            String csvRow = new StringBuilder().append(each.getPeriod())
                .append(CSV_DELIMITER)
                .append(Double.toString(each.getPacketLoss()))
                .append(CSV_DELIMITER)
                .append(Double.toString(each.getEnergyConsumption()))
                .toString();
            csvRows.add(csvRow);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(location, csvFileExists))) {
            csvRows.forEach(pw::println);
        }
    }

    @Override
    public void saveConfiguration(List<Mote> motes, int run, String strategyId) throws IOException {
        String csvFileName = strategyId + "Configurations.csv";
        String location = Paths.get(System.getProperty("user.dir"), "results", csvFileName)
            .toString();
        File csvOutputFile = new File(location);
        boolean csvFileExists = csvOutputFile.exists();

        List<String> csvRows = new ArrayList<>();
        if (csvFileExists == false) {
            csvOutputFile.createNewFile();

            String header = new StringBuilder().append("Run")
                .append(CSV_DELIMITER)
                .append("Link")
                .append(CSV_DELIMITER)
                .append("Power")
                .append(CSV_DELIMITER)
                .append("Distribution")
                .toString();
            csvRows.add(header);
        }

        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                String strLink = String.format("Link%1sto%2s", link.getSource(), link.getDest())
                    .replace(" ", "");
                String csvRow = new StringBuilder().append(run)
                    .append(CSV_DELIMITER)
                    .append(strLink)
                    .append(CSV_DELIMITER)
                    .append(link.getPower())
                    .append(CSV_DELIMITER)
                    .append(link.getDistribution())
                    .toString();
                csvRows.add(csvRow);
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(location, csvFileExists))) {
            csvRows.forEach(pw::println);
        }
    }

}