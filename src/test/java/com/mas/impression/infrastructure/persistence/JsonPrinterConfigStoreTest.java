package com.mas.impression.infrastructure.persistence;

import com.mas.impression.domain.model.PaperWidth;
import com.mas.impression.domain.model.PrinterConnectionConfig;
import com.mas.impression.domain.model.PrinterConnectionType;
import com.mas.impression.infrastructure.config.AgentDataProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonPrinterConfigStoreTest {

    @Test
    void loadReturnsEmptyWhenFileDoesNotExist(@TempDir Path tempDir) {
        JsonPrinterConfigStore store = storeIn(tempDir);

        assertEquals(Optional.empty(), store.load());
    }

    @Test
    void savedConfigCanBeLoadedBack(@TempDir Path tempDir) {
        JsonPrinterConfigStore store = storeIn(tempDir);
        PrinterConnectionConfig config = new PrinterConnectionConfig(
                PrinterConnectionType.NETWORK, PaperWidth.MM_80, "192.168.1.50", 9100, 2500, null, null);

        store.save(config);
        Optional<PrinterConnectionConfig> loaded = store.load();

        assertEquals(Optional.of(config), loaded);
        assertTrue(tempDir.resolve("printer-config.json").toFile().exists());
    }

    @Test
    void savingTwiceOverwritesThePreviousValue(@TempDir Path tempDir) {
        JsonPrinterConfigStore store = storeIn(tempDir);
        store.save(new PrinterConnectionConfig(
                PrinterConnectionType.NETWORK, PaperWidth.MM_58, "127.0.0.1", 9100, 1000, null, null));
        PrinterConnectionConfig updated = new PrinterConnectionConfig(
                PrinterConnectionType.NETWORK, PaperWidth.MM_80, "10.0.0.5", 9200, 4000, null, null);

        store.save(updated);

        assertEquals(Optional.of(updated), store.load());
    }

    private static JsonPrinterConfigStore storeIn(Path tempDir) {
        AgentDataProperties properties = new AgentDataProperties();
        properties.setDirectory(tempDir.toString());
        return new JsonPrinterConfigStore(new JsonMapper(), properties);
    }
}
