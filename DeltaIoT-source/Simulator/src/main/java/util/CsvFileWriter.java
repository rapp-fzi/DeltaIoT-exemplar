package util;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import deltaiot.services.Link;
import deltaiot.services.Mote;
import simulator.QoS;

public class CsvFileWriter implements ICSVWriter, IQOSWriter {
    private final static String CSV_DELIMITER = ";";

    private final Path baseLocation;

    public CsvFileWriter(Path baseLocation) {
        this.baseLocation = baseLocation;
    }

    @Override
    public void saveQoS(ArrayList<QoS> result, String strategyId) throws IOException {
        String csvFileName = strategyId + "Results.csv";
        Path location = baseLocation.resolve(csvFileName);

        CSVFormat.Builder builder = CSVFormat.Builder.create()
            .setDelimiter(CSV_DELIMITER)
            .setRecordSeparator("\r\n");
        if (!Files.exists(location)) {
            String[] HEADERS = { "Time", "Packet loss", "Energy consumption" };
            builder = builder.setHeader(HEADERS);
        }
        CSVFormat csvFormat = builder.build();

        try (Writer writer = Files.newBufferedWriter(location, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
                for (QoS each : result) {
                    printer.printRecord(each.getPeriod(), Double.toString(each.getPacketLoss()),
                            Double.toString(each.getEnergyConsumption()));
                }
            }
        }
    }

    @Override
    public void saveConfiguration(List<Mote> motes, int run, String strategyId) throws IOException {
        String csvFileName = strategyId + "Configurations.csv";
        Path location = baseLocation.resolve(csvFileName);

        CSVFormat.Builder builder = CSVFormat.Builder.create()
            .setDelimiter(CSV_DELIMITER)
            .setRecordSeparator("\r\n");
        if (!Files.exists(location)) {
            String[] HEADERS = { "Link", "Power", "Distribution" };
            builder = builder.setHeader(HEADERS);
        }
        CSVFormat csvFormat = builder.build();

        try (Writer writer = Files.newBufferedWriter(location, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
                for (Mote mote : motes) {
                    for (Link link : mote.getLinks()) {
                        String strLink = String.format("Link%1sto%2s", link.getSource(), link.getDest())
                            .replace(" ", "");
                        printer.printRecord(strLink, link.getPower(), link.getDistribution());
                    }
                }
            }
        }
    }

}