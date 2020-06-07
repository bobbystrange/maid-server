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
@RequestMapping(path = AppConfig.API_PREFIX + "/user")
public class UserController {
    private final UserService userService;

    /**
     * @api {delete} /user Delete user
     * @apiDescription it will delete all data associated the user
     * @apiName DeleteUser
     * @apiGroup User
     * @apiSuccess (Success 200 code = 0) code 0
     * @apiError (Error 200 code = 1) user not found, maybe deleted
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
     * @apiDescription Get user information
     * @apiName GetUser
     * @apiGroup User
     * @apiSuccess (Success 200 code = 0) code 0
     * @apiError (Error 200 code = 1) user not found, maybe deleted
     */
    @RequestMapping(method = RequestMethod.GET)
    public Mono<RestBody<UserView>> getUser(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> userService.getUser(exchange));
    }

    /**
     * @api {get} /user/avatar Get avatar
     * @apiDescription Get avatar base64 image
     * @apiName GetAvatar
     * @apiGroup User
     * @apiSuccess (Success 200) base64 image source
     * @apiError (Error 404 code = 1) user has no avatar
     * @apiSuccessExample {text} Success-Response:
     * data:image/gif;base64,xxx
     */
    @RequestMapping(path = {"/avatar"}, method = RequestMethod.GET)
    public Mono<String> getAvatar(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> userService.getAvatar(exchange));
    }

    /**
     * @api {put} /user Update user
     * @apiDescription Update user information
     * @apiName UpdateUser
     * @apiGroup User
     * @apiSuccess (Success 200 code = 0) code 0
     * @apiError (Error 200 code = 1) user not found, maybe deleted
     */
    @RequestMapping(method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<RestBody<?>> updateUser(
            @Valid @RequestBody Mono<UpdateUserQuery> query,
            ServerWebExchange exchange) {
        return query.map(it -> userService.updateUser(it, exchange));
    }

}
