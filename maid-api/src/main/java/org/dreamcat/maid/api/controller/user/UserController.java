package org.dreamcat.maid.api.controller.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppConfig;
import org.dreamcat.maid.api.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
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

    /**
     * @api {delete} /user Delete user
     * @apiDescription it will delete all data associated the user
     * @apiName DeleteUser
     * @apiGroup User
     * @apiSuccess {Number} code 0,
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public Mono<RestBody<?>> deleteUser(ServerWebExchange exchange) {
        //var violations = validator.validate(query);
        //if(!violations.isEmpty()){
        //    throw new BadRequestException(violations.toString());
        //}
        return Mono.fromCallable(() -> userService.deleteUser(exchange));
    }

    /**
     * @api {get} /user Get user
     * @apiDescription it will delete all data associated the user
     * @apiName GetUser
     * @apiGroup User
     * @apiSuccess {Number} code 0
     * @apiError (Error 404) {Number} code -1, user has been deleted
     */
    @RequestMapping(method = RequestMethod.GET)
    public Mono<RestBody<?>> getUser(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> userService.getUser(exchange));
    }

    /**
     * @api {get} /user/avatar Get avatar
     * @apiDescription get avatar base64 image
     * @apiName GetAvatar
     * @apiGroup User
     * @apiSuccess {Number} code 0
     * @apiError (Error 404) {Number} code -1, user has no avatar
     */
    @RequestMapping(path = {"/avatar"}, method = RequestMethod.GET)
    public Mono<String> getAvatar(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> userService.getAvatar(exchange));
    }

    /**
     * @api {put} /user Update user
     * @apiDescription update user
     * @apiName UpdateUser
     * @apiGroup User
     * @apiSuccess {Number} code 0
     * @apiError (Error 404) {Number} code -1, user has been deleted
     */
    @RequestMapping(method = RequestMethod.PUT)
    public Mono<RestBody<?>> updateUser(
            @Valid @RequestBody Mono<UpdateUserQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> userService.updateUser(it, exchange));
    }

}
