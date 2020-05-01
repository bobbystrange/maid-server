package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.NotFoundException;
import org.dreamcat.common.web.util.BeanCopierUtil;
import org.dreamcat.maid.api.controller.user.UpdateUserQuery;
import org.dreamcat.maid.api.controller.user.UserView;
import org.dreamcat.maid.api.service.FileOpService;
import org.dreamcat.maid.api.service.UserService;
import org.dreamcat.maid.cassandra.dao.AvatarDao;
import org.dreamcat.maid.cassandra.dao.UserDao;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

/**
 * Create by tuke on 2020/3/18
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final AvatarDao avatarDao;
    private final CommonService commonService;
    private final FileOpService fileOpService;

    @Override
    public RestBody<?> deleteUser(ServerWebExchange exchange) {
        fileOpService.removeNumerous("/", exchange);
        var uid = commonService.retrieveUid(exchange);
        userDao.deleteById(uid);
        avatarDao.delete(uid);
        return RestBody.ok();
    }

    @Override
    public RestBody<UserView> getUser(ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var user = userDao.findById(uid).orElse(null);
        if (user == null) {
            throw new NotFoundException("User doesn't exist, maybe deleted already");
        }

        var view = BeanCopierUtil.copy(user, UserView.class);
        view.setUsername(user.getName());
        return RestBody.ok(view);
    }

    @Override
    public String getAvatar(ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var avatar = avatarDao.find(uid);
        if (avatar == null) {
            throw new NotFoundException("Avatar doesn't exist");
        }
        return avatar.getAvatar();
    }

    @Override
    public RestBody<?> updateUser(UpdateUserQuery query, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var user = userDao.findById(uid).orElse(null);
        if (user == null) {
            throw new NotFoundException("User doesn't exist, maybe deleted already");
        }

        BeanCopierUtil.copy(query, user);
        user.setName(query.getUsername());
        userDao.save(user);
        return RestBody.ok();
    }

}
