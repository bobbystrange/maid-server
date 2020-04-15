package org.dreamcat.maid.api.controller.auth;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.maid.api.component.AuthComponent;
import org.dreamcat.maid.api.config.AppConfig;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

/**
 * Create by tuke on 2020/3/22
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/auth", method = {RequestMethod.POST},
        consumes = {MediaType.APPLICATION_JSON_VALUE})
public class AuthController {
    private AuthComponent<?> authComponent;

    @RequestMapping(path = "/register")
    public Mono<?> register(
            @Valid @RequestBody RegisterQuery query) {
        return Mono.fromCallable(() -> authComponent.register(query));
    }

    @RequestMapping(path = "/register/confirm")
    public Mono<?> registerConfirm(
            @Valid @RequestBody RegisterConfirmQuery query) {
        return Mono.fromCallable(() -> authComponent.registerConfirm(query));
    }

    @RequestMapping(path = "/login")
    public Mono<?> login(
            final ServerWebExchange exchange,
            @Valid @RequestBody LoginQuery query) {
        return Mono.fromCallable(() -> authComponent.login(query, token -> {
            exchange.getResponse().getHeaders().setBearerAuth(token);
        }));
    }

    @RequestMapping(path = "/password-reset", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<?> passwordReset(
            @Valid @RequestBody PasswordResetQuery query) {
        return Mono.fromCallable(() -> authComponent.passwordReset(query));

    }

    @RequestMapping(path = "/password-reset/confirm")
    public Mono<?> passwordResetConfirm(
            @Valid @RequestBody PasswordResetConfirmQuery query) {
        return Mono.fromCallable(() -> authComponent.passwordResetConfirm(query));
    }

    @RequestMapping(path = "/code/image")
    public Mono<?> fetchImageCode(
            @Valid @RequestBody ImageCodeQuery query) {
        return Mono.fromCallable(() -> authComponent.fetchImageCode(query));
    }
}
