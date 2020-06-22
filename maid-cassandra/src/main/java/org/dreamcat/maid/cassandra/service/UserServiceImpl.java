package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.web.asm.BeanCopierUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.NotFoundException;
import org.dreamcat.maid.api.controller.user.UpdateUserQuery;
import org.dreamcat.maid.api.controller.user.UserView;
import org.dreamcat.maid.api.service.UserService;
import org.dreamcat.maid.cassandra.dao.AvatarDao;
import org.dreamcat.maid.cassandra.dao.UserDao;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import static org.dreamcat.maid.cassandra.core.RestCodes.avatar_not_found;
import static org.dreamcat.maid.cassandra.core.RestCodes.user_not_found;

/**
 * Create by tuke on 2020/3/18
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final AvatarDao avatarDao;
    private final UserFileDao userFileDao;
    private final CommonService commonService;
    private final FileBatchService fileBatchService;

    @Override
    public RestBody<?> deleteUser(ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var user = userDao.findById(uid).orElse(null);
        if (user == null) {
            return RestBody.error(user_not_found, "user not found");
        }

        fileBatchService.deleteAllShareFile(uid);
        userFileDao.deleteByUid(uid);
        avatarDao.delete(uid);
        userDao.deleteById(uid);
        return RestBody.ok();
    }

    @Override
    public RestBody<UserView> getUser(ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var user = userDao.findById(uid).orElse(null);
        if (user == null) {
            return RestBody.error(user_not_found, "user not found");
        }

        var view = BeanCopierUtil.copy(user, UserView.class);
        return RestBody.ok(view);
    }

    @Override
    public String getAvatar(ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var avatar = avatarDao.find(uid);
        if (avatar == null) {
            throw new NotFoundException(avatar_not_found, "avatar not found");
        }
        return avatar.getAvatar();
    }

    @Override
    public RestBody<?> updateUser(UpdateUserQuery query, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var user = userDao.findById(uid).orElse(null);
        if (user == null) {
            return RestBody.error(user_not_found, "user not found");
        }

        BeanCopierUtil.copy(query, user);
        user.setName(query.getUsername());
        userDao.save(user);
        return RestBody.ok();
    }

}
