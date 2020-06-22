package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.exception.BreakException;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.web.asm.BeanCopierUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.config.AppProperties;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.file.FileView;
import org.dreamcat.maid.api.controller.file.IdNameView;
import org.dreamcat.maid.api.core.IdQuery;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.dreamcat.maid.cassandra.core.RestCodes.*;

/**
 * Create by tuke on 2020/4/14
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileChainService {
    private final UserFileDao userFileDao;
    private final CommonService commonService;
    private final AppProperties properties;
    private final CacheService cacheService;

    public String retrieveName(long uid, long fid) {
        if (fid == IdQuery.ROOT_ID) return "/";
        var pidAndName = cacheService.getPidAndName(uid, fid);
        if (pidAndName != null) {
            int ind = pidAndName.indexOf(':');
            return pidAndName.substring(ind + 1);
        }

        var file = userFileDao.findById(fid);
        if (file == null) return null;
        if (uid != file.getUid()) return null;
        cacheService.saveFidToPidAndName(file);
        return file.getName();
    }

    public String retrievePath(long uid, long fid) throws BreakException {
        if (fid == IdQuery.ROOT_ID) return "/";
        var path = cacheService.mapFidToPath(uid, fid);
        if (path != null) return path;

        var file = userFileDao.findById(fid);
        if (file == null) {
            throw new BreakException(RestBody.error(fid_not_found, "fid not found"));
        }
        return recurseQueryPath(file);
    }

    public String retrievePath(UserFileEntity file) throws BreakException {
        var uid = file.getUid();
        var fid = file.getId();

        if (fid == IdQuery.ROOT_ID) return "/";
        var path = cacheService.mapFidToPath(uid, fid);
        if (path != null) return path;

        return recurseQueryPath(file);
    }

    // /path/to/file_or_dir
    private String recurseQueryPath(UserFileEntity file) throws BreakException {
        var uid = file.getUid();
        var fid = file.getId();
        var name = file.getName();
        if (fid == IdQuery.ROOT_ID) {
            return "/";
        }

        cacheService.saveFidToPidAndName(file);
        var pid = file.getPid();

        var parentPath = cacheService.mapFidToPath(uid, pid);
        if (parentPath == null) {
            var parentFile = userFileDao.findById(pid);
            // dangling user_file entity
            if (parentFile == null) {
                userFileDao.delete(uid, pid, name);
                throw new BreakException(RestBody.error(fid_not_found, "fid not found"));
            }
            parentPath = recurseQueryPath(parentFile);
        }

        if (parentPath.equals("/")) return "/" + name;
        return parentPath + "/" + name;
    }

    // build dir tree
    public void recurseTree(FileView tree, UserFileEntity dir, int level, AtomicInteger batchSize) throws BreakException {
        if (level-- <= 0) return;

        var uid = dir.getUid();
        var pid = dir.getId();
        long count = userFileDao.countByPid(uid, pid);
        if (count == 0) return;
        if (count > properties.getFetchSize()) {
            throw new BreakException(RestBody.error(excessive_subitems, "excessive subitems"));
        }
        if (batchSize.addAndGet((int) -count) <= 0) {
            throw new BreakException(RestBody.error(excessive_items, "excessive items"));
        }

        var files = userFileDao.findByPid(uid, pid);
        tree.setItems(new ArrayList<>());
        for (var file : files) {
            var view = BeanCopierUtil.copy(file, FileView.class);
            view.setPath(FileUtil.normalize(tree.getPath() + "/" + file.getName()));
            if (commonService.isDirectory(file)) {
                recurseTree(view, file, level, batchSize);
            }
            batchSize.getAndDecrement();
            tree.getItems().add(view);
        }
    }

    // { path  => {name, path, ctime, mtime, digest, type, size, count} }
    public void recurseFlatDirTree(FileView view, Map<String, List<FileItemView>> map) {
        var path = view.getPath();
        var items = view.getItems();
        if (ObjectUtil.isEmpty(items)) {
            map.put(path, Collections.emptyList());
            return;
        }

        map.put(path, items.stream()
                .map(commonService::toFileItemView)
                .collect(Collectors.toList()));
        for (var i : items) {
            recurseFlatDirTree(i, map);
        }
    }

    public LinkedList<IdNameView> retrieveListPath(long uid, long fid) throws BreakException {
        if (fid == IdQuery.ROOT_ID) {
            var list = new LinkedList<IdNameView>();
            list.add(new IdNameView(fid, "/"));
            return list;
        }

        Long pid;
        String name;
        var pidAndName = cacheService.getPidAndName(uid, fid);
        if (pidAndName == null) {
            var file = userFileDao.findById(fid);
            if (file == null) {
                throw new BreakException(RestBody.error(fid_not_found, "fid not found"));
            }
            pid = file.getPid();
            name = file.getName();
            cacheService.saveFidToPidAndName(uid, fid, pid, name);
        } else {
            var ind = pidAndName.indexOf(':');
            pid = Long.parseLong(pidAndName.substring(0, ind));
            name = pidAndName.substring(ind + 1);
        }

        var list = retrieveListPath(uid, pid);
        list.addLast(new IdNameView(fid, name));
        return list;
    }

}
