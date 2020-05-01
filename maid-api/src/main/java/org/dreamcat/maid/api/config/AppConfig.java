package org.dreamcat.maid.api.config;

import org.dreamcat.common.web.handler.RestExceptionHandler;
import org.dreamcat.common.webflux.handler.BodyLoggingWebFilter;
import org.dreamcat.common.webflux.handler.RestErrorWebExceptionHandler;
import org.dreamcat.rita.auth.config.EnableRitaAuth;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Create by tuke on 2020/2/3
 */
@EnableRitaAuth
@EnableWebFlux
@Import({RestExceptionHandler.class, RestErrorWebExceptionHandler.class})
@EnableConfigurationProperties({AppProperties.class})
@Configuration
public class AppConfig {
    public static final String API_PREFIX = "/v1";

    @Bean
    public BodyLoggingWebFilter bodyLoggingWebFilter() {
        return new BodyLoggingWebFilter();
    }

    //@Bean
    //public HeaderLoggingWebFilter headerLoggingWebFilter() {
    //    return new HeaderLoggingWebFilter();
    //}
}
