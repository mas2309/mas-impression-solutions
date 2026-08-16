package com.mas.impression.infrastructure.config;

import com.mas.impression.domain.service.LogoRasterConverter;
import com.mas.impression.domain.service.TicketFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires framework-free domain services as Spring beans, keeping {@code domain} itself free of
 * Spring annotations.
 */
@Configuration
public class DomainServicesConfig {

    @Bean
    public TicketFormatter ticketFormatter() {
        return new TicketFormatter();
    }

    @Bean
    public LogoRasterConverter logoRasterConverter() {
        return new LogoRasterConverter();
    }
}
