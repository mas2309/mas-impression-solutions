package com.mas.impression.infrastructure.persistence;

import com.mas.impression.domain.model.PrintJob;
import com.mas.impression.domain.model.PrintStatus;
import com.mas.impression.infrastructure.config.AgentDataProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonLinesPrintHistoryStoreTest {

    @Test
    void recentReturnsEmptyWhenFileDoesNotExist(@TempDir Path tempDir) {
        JsonLinesPrintHistoryStore store = storeIn(tempDir);

        assertTrue(store.recent(10).isEmpty());
    }

    @Test
    void recordedJobsComeBackNewestFirst(@TempDir Path tempDir) {
        JsonLinesPrintHistoryStore store = storeIn(tempDir);
        store.record(job("job-1", LocalDateTime.of(2026, 8, 16, 10, 0)));
        store.record(job("job-2", LocalDateTime.of(2026, 8, 16, 10, 5)));
        store.record(job("job-3", LocalDateTime.of(2026, 8, 16, 10, 10)));

        List<PrintJob> recent = store.recent(10);

        assertEquals(List.of("job-3", "job-2", "job-1"), recent.stream().map(PrintJob::jobId).toList());
    }

    @Test
    void recentRespectsTheRequestedLimit(@TempDir Path tempDir) {
        JsonLinesPrintHistoryStore store = storeIn(tempDir);
        for (int i = 0; i < 5; i++) {
            store.record(job("job-" + i, LocalDateTime.of(2026, 8, 16, 10, i)));
        }

        assertEquals(2, store.recent(2).size());
    }

    private static PrintJob job(String jobId, LocalDateTime createdAt) {
        return new PrintJob(jobId, "POS-0001", PrintStatus.PRINTED, "ok", createdAt, createdAt);
    }

    private static JsonLinesPrintHistoryStore storeIn(Path tempDir) {
        AgentDataProperties properties = new AgentDataProperties();
        properties.setDirectory(tempDir.toString());
        return new JsonLinesPrintHistoryStore(new JsonMapper(), properties);
    }
}
