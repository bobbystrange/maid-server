package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.function.TriFunction;
import org.dreamcat.common.web.asm.BeanCopierUtil;
import org.dreamcat.common.web.core.RestBody;
import org.dreamcat.maid.api.controller.file.FileItemView;
import org.dreamcat.maid.api.controller.page.CategoryQuery;
import org.dreamcat.maid.api.controller.page.ShareItemView;
import org.dreamcat.maid.api.core.LastPageQuery;
import org.dreamcat.maid.api.core.LastPageView;
import org.dreamcat.maid.api.service.FilePageService;
import org.dreamcat.maid.cassandra.dao.ShareFileDao;
import org.dreamcat.maid.cassandra.dao.UserFileDao;
import org.dreamcat.maid.cassandra.entity.ShareFileEntity;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * Create by tuke on 2020/6/17
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FilePageServiceImpl implements FilePageService {
    private final CommonService commonService;
    private final UserFileDao userFileDao;
    private final ShareFileDao shareFileDao;

    @Override
    public RestBody<LastPageView<FileItemView>> category(CategoryQuery query, ServerWebExchange exchange) {
        var category = query.getCategory();
        var view = fetchPage(query, exchange,
                userFileDao::findLimit,
                it -> commonService.isFile(it) && category.belongs(it.getType()),
                UserFileEntity::getId,
                commonService::toFileItemView);
        return RestBody.ok(view);
    }

    @Override
    public RestBody<LastPageView<ShareItemView>> share(LastPageQuery query, ServerWebExchange exchange) {
        long uid = commonService.retrieveUid(exchange);

        var view = fetchPage(query, exchange,
                shareFileDao::findLimit,
                it -> true,
                ShareFileEntity::getId,
                it -> BeanCopierUtil.copy(it, ShareItemView.class));

        var expiredSids = view.getItems().stream()
                .filter(it -> {
                    var ctime = it.getCtime();
                    var ttl = it.getTtl();
                    // never expired
                    if (ttl == null) return false;
                    return System.currentTimeMillis() > ctime + ttl * 1000;
                })
                .map(ShareItemView::getId)
                .collect(Collectors.toList());
        if (!expiredSids.isEmpty()) {
            shareFileDao.deleteByIds(expiredSids);
            view.getItems().removeIf(it -> expiredSids.contains(it.getId()));
        }

        return RestBody.ok(view);
    }

    public <E, V> LastPageView<V> fetchPage(
            LastPageQuery query, ServerWebExchange exchange,
            TriFunction<Long, Integer, Long, List<E>> findLimit,
            Predicate<E> filter, ToLongFunction<E> idExtractor, Function<E, V> mapper) {
        long uid = commonService.retrieveUid(exchange);

        int size = query.getSize();
        var last = query.getLast();

        var items = new ArrayList<V>(size);
        var nextLast = last;
        var hasMore = true;

        int remainingSize = size;
        List<E> entities;
        while (remainingSize > 0) {
            entities = findLimit.apply(uid, size, nextLast);
            if (entities.isEmpty()) {
                hasMore = false;
                break;
            }
            var lastEntity = entities.get(entities.size() - 1);
            nextLast = idExtractor.applyAsLong(lastEntity);

            var filteredItems = entities.stream()
                    .filter(filter)
                    .map(mapper)
                    .collect(Collectors.toList());
            if (filteredItems.isEmpty()) continue;

            int filteredSize = filteredItems.size();
            if (filteredSize >= remainingSize) {
                var remainingItems = filteredItems.subList(0, remainingSize);
                items.addAll(remainingItems);
                break;
            }

            items.addAll(filteredItems);
            remainingSize -= filteredSize;
        }

        var view = new LastPageView<V>();
        view.setItems(items);
        view.setLast(nextLast);
        view.setHasMore(hasMore);
        return view;
    }

}
