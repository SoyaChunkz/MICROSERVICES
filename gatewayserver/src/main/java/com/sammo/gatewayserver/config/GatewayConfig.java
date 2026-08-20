package com.sammo.gatewayserver.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.time.Duration;
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
                                .addResponseHeader(RESPONSE_TIME_HEADER, LocalDateTime.now().toString())
                                .circuitBreaker(config -> config.setName("accountsCircuitBreaker")
                                        .setFallbackUri("forward:/contactSupport"))
                        )
                        .uri(ACCOUNTS_SERVICE))
                .route(p -> p
                        .path(LOANS_PATH)
                        .filters(f -> f.rewritePath(LOANS_REWRITE, SEGMENT)
                                .addResponseHeader(RESPONSE_TIME_HEADER, LocalDateTime.now().toString())
                                .retry(retryConfig -> retryConfig.setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true))
                        )
                        .uri(LOANS_SERVICE))
                .route(p -> p
                        .path(CARDS_PATH)
                        .filters(f -> f.rewritePath(CARDS_REWRITE, SEGMENT)
                                .addResponseHeader(RESPONSE_TIME_HEADER, LocalDateTime.now().toString()))
                        .uri(CARDS_SERVICE))
                .build();
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build()).build());
    }
}
