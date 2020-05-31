package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.exception.BreakException;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.web.asm.BeanCopierUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.file.FileView;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.dreamcat.maid.cassandra.core.RestCodes.excessive_items;
import static org.dreamcat.maid.cassandra.core.RestCodes.excessive_subitems;

/**
 * Create by tuke on 2020/4/14
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileChainService {
    private final UserFileDao userFileDao;
    private final CommonService commonService;

    // /path/to/file_or_dir
    public String retrievePath(UserFileEntity file) {
        var name = file.getName();
        if (name.equals("/")) {
            return "/";
        }

        var pid = file.getPid();
        var parentFile = userFileDao.findById(pid);
        var parentPath = retrievePath(parentFile);
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
        if (count > 1024) {
            throw new BreakException(RestBody.error(excessive_subitems, "excessive subitems"));
        }
        if (batchSize.addAndGet((int) -count) <= 0) {
            throw new BreakException(RestBody.error(excessive_items, "excessive items"));
        }

        var files = userFileDao.findAllByPid(uid, pid);
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

}
