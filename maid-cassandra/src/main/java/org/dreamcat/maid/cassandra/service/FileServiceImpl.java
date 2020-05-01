package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.common.web.exception.ForbiddenException;
import org.dreamcat.common.web.exception.NotFoundException;
import org.dreamcat.common.web.util.BeanCopierUtil;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.file.FileView;
import org.dreamcat.maid.api.core.PathLevelQuery;
import org.dreamcat.maid.api.service.FileService;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Create by tuke on 2020/3/18
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {
    private final UserFileDao userFileDao;
    private final CommonService commonService;

    @Override
    public RestBody<List<FileItemView>> list(String path, ServerWebExchange exchange) {
        commonService.checkPath(path);
        var uid = commonService.retrieveUid(exchange);
        var dir = userFileDao.find(uid, path);
        if (dir == null) {
            throw new NotFoundException("Directory " + path + " doesn't exist");
        }
        if (commonService.isFile(dir)) {
            throw new ForbiddenException(path + " is not a diretory");
        }

        var items = dir.getItems();
        if (ObjectUtil.isEmpty(items)) return RestBody.ok(new ArrayList<>());

        var files = userFileDao.findAll(uid, items, path).stream()
                .map(it -> {
                    var view = BeanCopierUtil.copy(it, FileItemView.class);
                    view.setName(FileUtil.basename(view.getPath()));
                    if (commonService.isDirectory(it)) {
                        view.setCount(it.getItems() != null ? it.getItems().size() : 0);
                    }
                    return view;
                }).collect(Collectors.toList());
        return RestBody.ok(files);
    }

    @Override
    public RestBody<FileView> tree(String path, int level, ServerWebExchange exchange) {
        var uid = commonService.retrieveUid(exchange);
        var dir = userFileDao.find(uid, path);
        if (dir == null) {
            throw new NotFoundException("Directory " + path + " doesn't exist");
        }
        if (commonService.isFile(dir)) {
            throw new ForbiddenException(path + " is not a diretory");
        }

        FileView tree = BeanCopierUtil.copy(dir, FileView.class);
        tree.setName(FileUtil.basename(dir.getPath()));
        recurseTree(tree, dir, uid, level);
        return RestBody.ok(tree);
    }

    // build dir tree
    public void recurseTree(FileView tree, UserFileEntity dir, UUID uid, int level) {
        if (level-- <= 0) return;

        var items = dir.getItems();
        if (ObjectUtil.isEmpty(items)) return;
        tree.setItems(new ArrayList<>());

        var path = dir.getPath();
        var files = userFileDao.findAll(uid, items, path);
        for (var file : files) {
            var view = BeanCopierUtil.copy(file, FileView.class);
            view.setName(FileUtil.basename(file.getPath()));
            recurseTree(view, file, uid, level);
            tree.getItems().add(view);
        }
    }

    @Override
    public RestBody<Map<String, List<FileItemView>>> flatTree(String path, ServerWebExchange exchange) {
        var result = tree(path, PathLevelQuery.MAX_DIR_LEVEL, exchange);
        if (result.getCode() != 0) {
            return RestBody.error(result.getMsg());
        }

        var view = result.getData();
        var map = new HashMap<String, List<FileItemView>>();
        recurseFlatDirTree(view, map);
        return RestBody.ok(map);
    }

    // { path  => {name, path, ctime, mtime, digest, type, size, count} }
    private void recurseFlatDirTree(FileView view, Map<String, List<FileItemView>> map) {
        var path = view.getPath();
        var items = view.getItems();
        if (ObjectUtil.isEmpty(items)) {
            map.put(path, new ArrayList<>());
            return;
        }

        map.put(path, items.stream()
                .map(it -> {
                    var itemView = BeanCopierUtil.copy(it, FileItemView.class);
                    if (itemView.getType() == null) {
                        var v = it.getItems();
                        itemView.setCount(v != null ? v.size() : 0);
                    }
                    return itemView;
                })
                .collect(Collectors.toList()));

        for (var i : items) {
            recurseFlatDirTree(i, map);
        }
    }

}
