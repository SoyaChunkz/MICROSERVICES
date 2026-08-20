package com.sammo.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class ResponseTraceFilter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseTraceFilter.class);

    @Autowired
    FilterUtility filterUtility;

    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) ->
                chain.filter(exchange)
                        .then(Mono.fromRunnable(() -> {
                            String correlationId = filterUtility.getCorrelationId(exchange.getRequest().getHeaders());

                            if (!(exchange.getResponse().getHeaders().containsHeader(FilterUtility.CORRELATION_ID))) {
                                logger.debug("Adding correlation id to outbound response headers: {}", correlationId);
                                exchange.getResponse().getHeaders().set(FilterUtility.CORRELATION_ID, correlationId);
                            }
                        }));
    }
}
