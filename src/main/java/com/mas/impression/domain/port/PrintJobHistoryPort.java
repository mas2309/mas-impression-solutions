package com.mas.impression.domain.port;

import com.mas.impression.domain.model.PrintJob;

import java.util.List;

/**
 * Records print attempts for later review. Best-effort by design: a history-recording failure
 * must never fail the print itself, so implementations should not throw for normal I/O hiccups.
 */
public interface PrintJobHistoryPort {

    void record(PrintJob job);

    List<PrintJob> recent(int limit);
}
