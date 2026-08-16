package com.mas.impression.application.port.in;

import com.mas.impression.application.dto.PrintJobResponse;

import java.util.List;

public interface GetPrintHistoryUseCase {

    List<PrintJobResponse> getRecent(int limit);
}
