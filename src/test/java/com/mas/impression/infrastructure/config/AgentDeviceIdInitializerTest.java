package com.mas.impression.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentDeviceIdInitializerTest {

    @Test
    void generatesAndPersistsDeviceIdWhenNothingPersisted(@TempDir Path tempDir) throws IOException {
        AgentDeviceIdInitializer initializer = new AgentDeviceIdInitializer(dataPropertiesIn(tempDir));

        initializer.resolveDeviceId();

        assertFalse(initializer.getDeviceId().isBlank());
        Path file = tempDir.resolve("device-id.txt");
        assertTrue(Files.exists(file));
        assertEquals(initializer.getDeviceId(), Files.readString(file, StandardCharsets.UTF_8).trim());
    }

    @Test
    void reusesPersistedDeviceIdOverRestarts(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("device-id.txt"), "persisted-device-id", StandardCharsets.UTF_8);
        AgentDeviceIdInitializer initializer = new AgentDeviceIdInitializer(dataPropertiesIn(tempDir));

        initializer.resolveDeviceId();

        assertEquals("persisted-device-id", initializer.getDeviceId());
    }

    @Test
    void generatesADifferentDeviceIdPerInstallWhenNothingPersisted(@TempDir Path tempDirA, @TempDir Path tempDirB) {
        AgentDeviceIdInitializer first = new AgentDeviceIdInitializer(dataPropertiesIn(tempDirA));
        AgentDeviceIdInitializer second = new AgentDeviceIdInitializer(dataPropertiesIn(tempDirB));

        first.resolveDeviceId();
        second.resolveDeviceId();

        assertNotEquals(first.getDeviceId(), second.getDeviceId());
    }

    private static AgentDataProperties dataPropertiesIn(Path directory) {
        AgentDataProperties properties = new AgentDataProperties();
        properties.setDirectory(directory.toString());
        return properties;
    }
}
