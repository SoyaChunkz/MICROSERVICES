package com.sammo.gatewayserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping("/contactSupport")
    public Mono<String> contactSupport () {
        logger.warn("Gateway fallback endpoint invoked");
        return Mono.just("An error occurred. Please try after sometime or contact support team!!!");
    }
}
