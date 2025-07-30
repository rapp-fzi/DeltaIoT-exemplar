package util;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonQOSWriter implements IQOSWriter {
    private final Path baseLocation;

    public JsonQOSWriter(Path baseLocation) {
        this.baseLocation = baseLocation;
    }

    @Override
    public void saveQoS(QoSResult qosResult) throws IOException {
        String strategyId = qosResult.getStrategyName();
        Path strategyFolder = baseLocation.resolve(strategyId);
        Files.createDirectories(strategyFolder);

        Path location = strategyFolder.resolve("Results.json");

        Gson gson = new GsonBuilder().serializeNulls()
            .setPrettyPrinting()
            .create();
        try (Writer writer = Files.newBufferedWriter(location)) {
            gson.toJson(qosResult, writer);
        }
    }
}
