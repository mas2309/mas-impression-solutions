package com.mas.impression.application.service;

import com.mas.impression.application.dto.PrintJobResponse;
import com.mas.impression.application.port.in.GetPrintHistoryUseCase;
import com.mas.impression.domain.model.PrintJob;
import com.mas.impression.domain.port.PrintJobHistoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrintHistoryService implements GetPrintHistoryUseCase {

    private final PrintJobHistoryPort history;

    public PrintHistoryService(PrintJobHistoryPort history) {
        this.history = history;
    }

    @Override
    public List<PrintJobResponse> getRecent(int limit) {
        return history.recent(limit).stream().map(PrintHistoryService::toResponse).toList();
    }

    private static PrintJobResponse toResponse(PrintJob job) {
        return new PrintJobResponse(
                job.jobId(), job.saleNumber(), job.status(), job.message(), job.createdAt(), job.printedAt());
    }
}
