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

public class CsvFileWriter {

    public final static String CSV_DELIMITER = ";";

    public static void saveQoS(ArrayList<QoS> result, String strategyId) {
        String csvFileName = strategyId + "Results.csv";
        String location = Paths.get(System.getProperty("user.dir"), "results", csvFileName)
            .toString();
        File csvOutputFile = new File(location);

        boolean csvFileExists = csvOutputFile.exists();

        List<String> csvRows = new ArrayList<>();
        if (csvFileExists == false) {
            try {
                csvOutputFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logAndSaveConfiguration(List<Mote> motes, int run, String strategyId) {
        String csvFileName = strategyId + "Configurations.csv";
        String location = Paths.get(System.getProperty("user.dir"), "results", csvFileName)
            .toString();
        File csvOutputFile = new File(location);

        boolean csvFileExists = csvOutputFile.exists();

        List<String> csvRows = new ArrayList<>();
        if (csvFileExists == false) {
            try {
                csvOutputFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

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

        System.out.println("******** Network configuration of " + run + " *******");

        for (Mote mote : motes) {
            for (Link link : mote.getLinks()) {
                System.out.println(link.toString());

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

        System.out.println("******** END *******");

        try (PrintWriter pw = new PrintWriter(new FileWriter(location, csvFileExists))) {
            csvRows.forEach(pw::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}