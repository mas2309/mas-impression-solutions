package com.mas.impression.infrastructure.notification;

import com.mas.impression.infrastructure.config.BackendProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;

/**
 * Best-effort "this sale was printed" notification back to the ERP backend. Deliberately tolerant
 * of everything: no base URL configured, no sale id on the request, the backend being unreachable,
 * or the endpoint not existing yet (it doesn't, as of this milestone — see the backend backlog
 * item to add {@code PUT /api/v1/sales/{saleId}/print-status}). None of this may ever fail the
 * print itself, which is why every failure path here just logs and returns.
 */
@Component
public class BackendNotifierClient {

    private static final Logger LOG = LoggerFactory.getLogger(BackendNotifierClient.class);
    private static final String PRINT_STATUS_PATH = "/api/v1/sales/{saleId}/print-status";

    private final BackendProperties properties;
    private final RestClient restClient;

    public BackendNotifierClient(BackendProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().requestFactory(requestFactory(properties)).build();
    }

    public void notifyPrinted(Long saleId, LocalDateTime printedAt) {
        if (!properties.isConfigured() || saleId == null) {
            return;
        }
        try {
            restClient.put()
                    .uri(properties.getBaseUrl() + PRINT_STATUS_PATH, saleId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PrintStatusPayload(true, printedAt))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            LOG.debug("No se pudo notificar al backend la impresión de la venta {}: {}", saleId, ex.getMessage());
        }
    }

    private static SimpleClientHttpRequestFactory requestFactory(BackendProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMillis());
        factory.setReadTimeout(properties.getReadTimeoutMillis());
        return factory;
    }

    private record PrintStatusPayload(boolean printed, LocalDateTime printedAt) {
    }
}
