package com.mas.impression.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentTokenInitializerTest {

    @Test
    void generatesAndPersistsTokenWhenNothingConfiguredOrPersisted(@TempDir Path tempDir) throws IOException {
        AgentSecurityProperties securityProperties = new AgentSecurityProperties();
        AgentDataProperties dataProperties = dataPropertiesIn(tempDir);

        new AgentTokenInitializer(securityProperties, dataProperties).resolveToken();

        assertFalse(securityProperties.getToken().isBlank());
        Path file = tempDir.resolve("agent-token.txt");
        assertTrue(Files.exists(file));
        assertEquals(securityProperties.getToken(), Files.readString(file, StandardCharsets.UTF_8).trim());
    }

    @Test
    void reusesPersistedTokenOverAnyOtherValue(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("agent-token.txt"), "persisted-token", StandardCharsets.UTF_8);
        AgentSecurityProperties securityProperties = new AgentSecurityProperties();
        AgentDataProperties dataProperties = dataPropertiesIn(tempDir);

        new AgentTokenInitializer(securityProperties, dataProperties).resolveToken();

        assertEquals("persisted-token", securityProperties.getToken());
    }

    @Test
    void persistsAnExplicitlyConfiguredTokenWhenNoFileExistsYet(@TempDir Path tempDir) throws IOException {
        AgentSecurityProperties securityProperties = new AgentSecurityProperties();
        securityProperties.setToken("explicit-token");
        AgentDataProperties dataProperties = dataPropertiesIn(tempDir);

        new AgentTokenInitializer(securityProperties, dataProperties).resolveToken();

        assertEquals("explicit-token", securityProperties.getToken());
        Path file = tempDir.resolve("agent-token.txt");
        assertEquals("explicit-token", Files.readString(file, StandardCharsets.UTF_8).trim());
    }

    private static AgentDataProperties dataPropertiesIn(Path tempDir) {
        AgentDataProperties properties = new AgentDataProperties();
        properties.setDirectory(tempDir.toString());
        return properties;
    }
}
