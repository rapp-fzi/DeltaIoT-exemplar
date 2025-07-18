package domain;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.FileHandler;

public class FileProfile implements Profile<Double> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileProfile.class);

    private List<Double> values;
    private Double defaultValue;

    public FileProfile(String relPath, Double defaultValue) {
        this.values = FileHandler.parseNumberList(relPath);
        this.defaultValue = defaultValue;
    }

    @Override
    public Double get(int runNumber) {
        if (runNumber >= 0 && runNumber < values.size()) {
            double val = values.get(runNumber);
            return val;
        } else {
            LOGGER.error("Unknown value data for run {} returning default ({}).", runNumber, defaultValue);
            return defaultValue;
        }

    }

}
