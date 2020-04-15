package org.dreamcat.maid.api.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.core.IdQuery;
import org.dreamcat.maid.api.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

/**
 * Create by tuke on 2020/2/3
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = AppConfig.API_PREFIX + "/user",
        consumes = {MediaType.APPLICATION_JSON_VALUE})
public class UserController {
    private final UserService userService;

    @RequestMapping(method = RequestMethod.POST)
    public Mono<?> createUser(@Valid @RequestBody Mono<CreateUserQuery> query) {
        return query.map(userService::createUser);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public Mono<?> deleteUser(@Valid @RequestBody Mono<IdQuery> query) {
        //var violations = validator.validate(query);
        //if(!violations.isEmpty()){
        //    throw new BadRequestException(violations.toString());
        //}
        return query.map(it -> userService.deleteUser(it.getId())).doOnError(
                WebExchangeBindException.class, e -> {
                    log.error(e.getMessage());
                });
        //return Mono.fromCallable(() -> userService.deleteUser(query.getId()));
    }

    @RequestMapping(method = RequestMethod.GET)
    public Mono<?> getUser(@Valid @RequestBody Mono<IdQuery> query) {
        return query.map(it -> userService.getUser(it.getId()));
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Mono<?> updateUser(@Valid @RequestBody Mono<UpdateUserQuery> query) {
        return query.map(userService::updateUser);
    }

}
