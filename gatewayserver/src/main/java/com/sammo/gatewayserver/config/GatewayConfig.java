package com.sammo.gatewayserver.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class GatewayConfig {

    private static final String ACCOUNTS_SERVICE = "lb://ACCOUNTS";
    private static final String LOANS_SERVICE = "lb://LOANS";
    private static final String CARDS_SERVICE = "lb://CARDS";

    private static final String ACCOUNTS_PATH = "/sammobank/accounts/**";
    private static final String LOANS_PATH = "/sammobank/loans/**";
    private static final String CARDS_PATH = "/sammobank/cards/**";

    private static final String ACCOUNTS_REWRITE = "/sammobank/accounts/(?<segments>.*)";
    private static final String LOANS_REWRITE = "/sammobank/loans/(?<segments>.*)";
    private static final String CARDS_REWRITE = "/sammobank/cards/(?<segments>.*)";

    private static final String SEGMENT = "/${segments}";

    private static final String RESPONSE_TIME_HEADER = "X-Response-Time";



    @Bean
    public RouteLocator sammoBankRouteConfig (RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(p -> p
                        .path(ACCOUNTS_PATH)
                        .filters(f -> f.rewritePath(ACCOUNTS_REWRITE, SEGMENT)
                                .addResponseHeader(RESPONSE_TIME_HEADER, LocalDateTime.now().toString()))
                        .uri(ACCOUNTS_SERVICE))
                .route(p -> p
                        .path(LOANS_PATH)
                        .filters(f -> f.rewritePath(LOANS_REWRITE, SEGMENT)
                                .addResponseHeader(RESPONSE_TIME_HEADER, LocalDateTime.now().toString()))
                        .uri(LOANS_SERVICE))
                .route(p -> p
                        .path(CARDS_PATH)
                        .filters(f -> f.rewritePath(CARDS_REWRITE, SEGMENT)
                                .addResponseHeader(RESPONSE_TIME_HEADER, LocalDateTime.now().toString()))
                        .uri(CARDS_SERVICE))
                .build();
    }
}
