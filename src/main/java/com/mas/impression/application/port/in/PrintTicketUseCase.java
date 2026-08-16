package com.mas.impression.application.port.in;

import com.mas.impression.application.dto.PrintTicketRequest;
import com.mas.impression.application.dto.PrintTicketResponse;

public interface PrintTicketUseCase {

    PrintTicketResponse printTicket(PrintTicketRequest request);
}
