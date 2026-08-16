package com.mas.impression.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DomainServicesConfigTest {

    @Test
    void providesTicketFormatterBean() {
        assertNotNull(new DomainServicesConfig().ticketFormatter());
    }
}
