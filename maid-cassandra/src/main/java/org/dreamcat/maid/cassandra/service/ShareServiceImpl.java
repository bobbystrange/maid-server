package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.core.Pair;
import org.dreamcat.common.exception.BreakException;
import org.dreamcat.common.web.asm.BeanCopierUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.common.web.exception.InternalServerErrorException;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.file.IdNameView;
import org.dreamcat.maid.api.controller.share.GetShareFileQuery;
import org.dreamcat.maid.api.controller.share.ShareFileInfoView;
import org.dreamcat.maid.api.core.IdQuery;
import org.dreamcat.maid.api.service.ShareService;
import org.dreamcat.maid.cassandra.core.RestCodes;
import org.dreamcat.maid.cassandra.dao.ShareFileDao;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.ShareFileEntity;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.dreamcat.maid.cassandra.hub.InstanceService;
import org.dreamcat.maid.cassandra.hub.RestService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.dreamcat.maid.cassandra.core.RestCodes.*;

/**
 * Create by tuke on 2020/5/31
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ShareServiceImpl implements ShareService {
    private final UserFileDao userFileDao;
    private final ShareFileDao shareFileDao;
    private final CommonService commonService;
    private final CacheService cacheService;
    private final FileChainService fileChainService;
    private final InstanceService instanceService;
    private final RestService restService;

    @Override
    public RestBody<ShareFileInfoView> file(GetShareFileQuery query) {
        UserFileEntity file;
        ShareFileEntity shareFile;
        try {
            var pair = locate(query);
            shareFile = pair.first();
            file = pair.second();
        } catch (BreakException e) {
            return e.getData();
        }
        var view = BeanCopierUtil.copy(file, ShareFileInfoView.class);
        if (commonService.isDirectory(file)) {
            long count = userFileDao.countByPid(file.getUid(), file.getId());
            view.setCount(count);
        }
        view.setTtl(shareFile.getTtl());
        view.setStime(shareFile.getCtime());
        return RestBody.ok(view);
    }

    @Override
    public RestBody<List<FileItemView>> list(GetShareFileQuery query) {
        UserFileEntity dir;
        try {
            dir = locate(query).second();
        } catch (BreakException e) {
            return e.getData();
        }
        if (commonService.isFile(dir)) {
            return RestBody.error(shared_file_not_diretory, "file not diretory");
        }
        long uid = dir.getUid();
        long pid = dir.getId();

        long count = userFileDao.countByPid(uid, pid);
        if (count > 1024) {
            return RestBody.error(sid_excessive_subitems, "excessive subitems");
        }
        var files = userFileDao.findByPid(uid, pid);
        var views = files.stream()
                .map(commonService::toFileItemView)
                .collect(Collectors.toList());
        return RestBody.ok(views);
    }

    @Override
    public RestBody<List<IdNameView>> listPath(GetShareFileQuery query) {
        var sid = query.getSid();
        var password = query.getPassword();
        var fid = query.getFid();

        UserFileEntity root;
        try {
            root = checkSid(sid, password).second();
        } catch (BreakException e) {
            return e.getData();
        }

        long uid = root.getUid();
        if (fid == null) fid = root.getId();

        try {
            return RestBody.ok(retrieveListPath(root, uid, fid));
        } catch (BreakException e) {
            return e.getData();
        }
    }

    @Override
    public RestBody<String> download(GetShareFileQuery query, boolean attachment) {
        UserFileEntity file;
        try {
            file = locate(query).second();
        } catch (BreakException e) {
            return e.getData();
        }
        if (commonService.isDirectory(file)) {
            return RestBody.error(shared_file_not_file, "file not file");
        }

        var digest = file.getDigest();
        var domain = instanceService.findMostIdleDomainAddress(digest);
        if (domain == null) {
            log.error("No available instances for download {}", digest);
            throw new InternalServerErrorException("no available instances");
        }
        var name = file.getName();
        var type = file.getType();
        String url;
        if (attachment) {
            url = restService.concatDownloadURL(digest, domain, name, type);
        } else {
            url = restService.concatFetchURL(digest, domain, name, type);
        }
        return RestBody.ok(url);

    }

    private Pair<ShareFileEntity, UserFileEntity> locate(GetShareFileQuery query) throws BreakException {
        var sid = query.getSid();
        var password = query.getPassword();
        var fid = query.getFid();

        var pair = checkSid(sid, password);
        var shareFile = pair.first();
        var userFile = pair.second();
        if (fid != null) {
            userFile = checkFid(userFile, fid);
        }
        return new Pair<>(shareFile, userFile);
    }

    private Pair<ShareFileEntity, UserFileEntity> checkSid(long sid, String password) throws BreakException {
        var shareFile = shareFileDao.findById(sid);
        if (shareFile == null) {
            throw new BreakException(RestBody.error(sid_not_found, "sid not found"));
        }
        String rawPassword = shareFile.getPassword();
        if (rawPassword != null) {
            if (password == null) {
                throw new BreakException(RestBody.error(sid_require_password, "sid require password"));
            }

            if (!Objects.equals(password, rawPassword)) {
                throw new BreakException(RestBody.error(sid_wrong_password, "sid wrong password"));
            }
        }

        Long ttl = shareFile.getTtl();
        if (ttl != null) {
            if (System.currentTimeMillis() > shareFile.getCtime() + ttl) {
                shareFileDao.deleteById(sid);
                throw new BreakException(RestBody.error(RestCodes.expired_sid, "expired sid"));
            }
        }

        var fid = shareFile.getFid();
        var userFile = userFileDao.findById(fid);
        if (userFile == null || !Objects.equals(userFile.getName(), shareFile.getName())) {
            shareFileDao.deleteById(sid);
            // already renamed or removed
            throw new BreakException(RestBody.error(sid_already_invalid, "sid already invalid"));
        }
        return new Pair<>(shareFile, userFile);
    }

    private UserFileEntity checkFid(UserFileEntity root, long fid) throws BreakException {
        if (root.getId() == fid) return root;
        long uid = root.getUid();
        long rootId = root.getId();

        var file = userFileDao.findById(fid);
        if (file == null) {
            throw new BreakException(RestBody.error(shared_file_not_found, "file not found"));
        }

        if (uid != file.getUid()) {
            throw new ForbiddenException(insufficient_permissions, "insufficient permissions");
        }

        var matched = false;
        Long id = fid;
        while (true) {
            id = fileChainService.retrievePidAndName(uid, id).first();
            if (id == null) break;

            if (rootId == id) {
                matched = true;
                break;
            }
        }

        if (!matched) {
            throw new BreakException(RestBody.error(shared_file_not_shared, "file not shared"));
        }

        return file;
    }

    private LinkedList<IdNameView> retrieveListPath(UserFileEntity root, long uid, long fid) throws BreakException {
        if (fid == root.getId()) {
            var list = new LinkedList<IdNameView>();
            list.add(new IdNameView(fid, "/"));
            return list;
        }
        if (fid == IdQuery.ROOT_ID) {
            throw new BreakException(RestBody.error(shared_file_not_shared, "file not shared"));
        }

        Long pid;
        String name;
        var pidAndName = cacheService.getPidAndName(uid, fid);
        if (pidAndName == null) {
            var file = userFileDao.findById(fid);
            if (file == null) {
                throw new BreakException(RestBody.error(shared_file_not_found, "file not found"));
            }
            pid = file.getPid();
            name = file.getName();
            cacheService.saveFidToPidAndName(uid, fid, pid, name);
        } else {
            var ind = pidAndName.indexOf(':');
            pid = Long.parseLong(pidAndName.substring(0, ind));
            name = pidAndName.substring(ind + 1);
        }

        var list = retrieveListPath(root, uid, pid);
        list.addLast(new IdNameView(fid, name));
        return list;
    }
}
