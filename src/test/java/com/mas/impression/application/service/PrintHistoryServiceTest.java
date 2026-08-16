package com.mas.impression.application.service;

import com.mas.impression.application.dto.PrintJobResponse;
import com.mas.impression.domain.model.PrintJob;
import com.mas.impression.domain.model.PrintStatus;
import com.mas.impression.domain.port.PrintJobHistoryPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrintHistoryServiceTest {

    @Test
    void mapsDomainJobsToResponses() {
        PrintJobHistoryPort history = mock(PrintJobHistoryPort.class);
        LocalDateTime now = LocalDateTime.of(2026, 8, 16, 10, 30);
        when(history.recent(5)).thenReturn(
                List.of(new PrintJob("job-1", "POS-0001", PrintStatus.PRINTED, "ok", now, now)));

        List<PrintJobResponse> responses = new PrintHistoryService(history).getRecent(5);

        assertEquals(1, responses.size());
        assertEquals("job-1", responses.get(0).jobId());
        assertEquals(PrintStatus.PRINTED, responses.get(0).status());
    }
}
