package org.dreamcat.maid.api.config;

import org.dreamcat.common.web.handler.RestExceptionHandler;
import org.dreamcat.common.web.mail.MailTransmitter;
import org.dreamcat.common.web.template.ThymeleafProcesser;
import org.dreamcat.common.webflux.handler.BodyLoggingWebFilter;
import org.dreamcat.common.webflux.handler.RestErrorWebExceptionHandler;
import org.dreamcat.common.webflux.security.EnableJwtReactiveSecurity;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Create by tuke on 2020/2/3
 */
@EnableWebFluxSecurity
@EnableJwtReactiveSecurity
@EnableWebFlux
@Import({RestExceptionHandler.class, RestErrorWebExceptionHandler.class,
        MailTransmitter.class, ThymeleafProcesser.class})
@EnableConfigurationProperties({AppProperties.class, AuthProperties.class})
@Configuration
public class AppConfig {
    public static final String API_PREFIX = "/api/v1";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public BodyLoggingWebFilter bodyLoggingWebFilter() {
        return new BodyLoggingWebFilter();
    }

    //@Bean
    //public HeaderLoggingWebFilter headerLoggingWebFilter() {
    //    return new HeaderLoggingWebFilter();
    //}
}
