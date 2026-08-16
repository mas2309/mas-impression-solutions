package com.mas.impression.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where the ERP backend lives, for the best-effort post-print notification. Left blank by
 * default: the backend doesn't have the print-status endpoint yet (tracked as a follow-up), and
 * printing itself must keep working with this unset.
 */
@ConfigurationProperties(prefix = "agent.backend")
public class BackendProperties {

    private String baseUrl = "";
    private int connectTimeoutMillis = 2000;
    private int readTimeoutMillis = 2000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank();
    }
}
