package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.exception.BreakException;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.BadRequestException;
import org.dreamcat.common.web.exception.InternalServerErrorException;
import org.dreamcat.maid.api.controller.file.FileInfoView;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.share.GetShareFileQuery;
import org.dreamcat.maid.api.service.ShareService;
import org.dreamcat.maid.cassandra.core.RestCodes;
import org.dreamcat.maid.cassandra.dao.ShareFileDao;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.dreamcat.maid.cassandra.hub.InstanceService;
import org.dreamcat.maid.cassandra.hub.RestService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
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
    private final PasswordEncoder passwordEncoder;
    private final InstanceService instanceService;
    private final RestService restService;

    @Override
    public RestBody<FileInfoView> file(GetShareFileQuery query) {
        UserFileEntity file;
        try {
            file = locate(query);
        } catch (BreakException e) {
            return e.getData();
        }
        return RestBody.ok(commonService.toFileInfoView(file));
    }

    @Override
    public RestBody<List<FileItemView>> list(GetShareFileQuery query) {
        UserFileEntity dir;
        try {
            dir = locate(query);
        } catch (BreakException e) {
            return e.getData();
        }
        if (commonService.isFile(dir)) {
            return RestBody.error(shared_path_not_diretory, "shared fid not diretory");
        }
        var uid = dir.getUid();
        var pid = dir.getId();

        long count = userFileDao.countByPid(uid, pid);
        if (count > 1024) {
            return RestBody.error(sid_excessive_subitems, "sid excessive subitems");
        }
        var files = userFileDao.findAllByPid(uid, pid);
        var views = files.stream()
                .map(commonService::toFileItemView)
                .collect(Collectors.toList());
        return RestBody.ok(views);
    }

    @Override
    public RestBody<String> download(GetShareFileQuery query, boolean attachment) {
        UserFileEntity file;
        try {
            file = locate(query);
        } catch (BreakException e) {
            return e.getData();
        }
        if (commonService.isDirectory(file)) {
            return RestBody.error(shared_path_not_file, "shared fid not file");
        }

        var digest = file.getDigest();
        var domain = instanceService.findMostIdleDomainAddress(digest);
        if (domain == null) {
            log.error("No available instances for download {}", digest);
            throw new InternalServerErrorException(download_no_available_instances, "no available instances");
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

    private UserFileEntity locate(GetShareFileQuery query) throws BreakException {
        var sid = query.getSid();
        var password = query.getPassword();
        var path = query.getPath();

        var userFile = checkSid(sid, password);
        if (path == null) {
            return userFile;
        }
        return checkPath(userFile, path);
    }

    private UserFileEntity checkSid(long sid, String password) throws BreakException {
        var shareFile = shareFileDao.findById(sid);
        if (shareFile == null) {
            throw new BreakException(RestBody.error(sid_not_found, "sid not found"));
        }
        String encodedPassword = shareFile.getPassword();
        if (encodedPassword != null) {
            if (password == null) {
                throw new BreakException(RestBody.error(sid_require_password, "sid require password"));
            }

            if (!passwordEncoder.matches(password, encodedPassword)) {
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
        if (userFile == null) {
            shareFileDao.deleteById(sid);
            throw new BreakException(RestBody.error(sid_already_invalid, "sid already invalid"));
        }
        return userFile;
    }

    private UserFileEntity checkPath(UserFileEntity root, String path) throws BreakException {
        if (!path.matches("^/.{0,8192}[^/]$")) {
            throw new BadRequestException("invalid path");
        }

        if (path.equals("/")) return root;

        var uid = root.getUid();
        var names = Arrays.stream(path.split("/"))
                .dropWhile(String::isEmpty)
                .collect(Collectors.toList());

        UserFileEntity file = root;
        for (String name : names) {
            if (commonService.isFile(file)) {
                throw new BreakException(RestBody.error(shared_path_not_diretory, "shared fid not diretory"));
            }
            file = userFileDao.findByPidAndName(uid, file.getId(), name);
            if (file == null) {
                throw new BreakException(RestBody.error(shared_path_not_found, "shared fid not found"));
            }
        }
        return file;
    }

}
