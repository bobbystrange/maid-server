package org.dreamcat.maid.cassandra.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.maid.cassandra.config.RedisConfig;
import org.dreamcat.maid.cassandra.entity.UserFileEntity;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Create by tuke on 2020/6/9
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CacheService {
    private static final String fid2path_lua_script =
            "local function recurseSearch(fid)" +
                    " if fid == 0 then return '/' end" +
                    " local pidAndName = redis.call('hget', KEYS[1], fid)" +
                    " if (not pidAndName) then return nil end" +
                    " local ind = string.find(pidAndName, ':')" +
                    " local pid = tonumber(string.sub(pidAndName, 0, ind - 1))" +
                    " local name = string.sub(pidAndName, ind + 1)" +
                    " local dirname = recurseSearch(pid)" +
                    " if dirname == '/' then return dirname .. name" +
                    " else return dirname .. '/' .. name" +
                    " end" +
                    " end" +
                    " return recurseSearch(KEYS[2])";
    private static final DefaultRedisScript<String> fid2path_redis_script = new DefaultRedisScript<>(
            fid2path_lua_script, String.class);
    private final StringRedisTemplate redisTemplate;
    private final RedisConfig redisConfig;

    public String mapFidToPath(long uid, long fid) {
        var key = redisConfig.getMapFidToPidAndNameHashPrefix() + ":" + uid;
        return redisTemplate.execute(fid2path_redis_script,
                Arrays.asList(key, String.valueOf(fid)));
    }

    public void saveFidToPidAndName(UserFileEntity file) {
        saveFidToPidAndName(file.getUid(), file.getId(), file.getPid(), file.getName());
    }

    public void saveFidToPidAndName(long uid, long fid, long pid, String name) {
        var key = redisConfig.getMapFidToPidAndNameHashPrefix() + ":" + uid;

        var pidAndName = String.format("%d:%s", pid, name);
        redisTemplate.boundHashOps(key).put(String.valueOf(fid), pidAndName);
    }

    public void deleteFidToPidAndName(long uid, long fid) {
        var key = redisConfig.getMapFidToPidAndNameHashPrefix() + ":" + uid;
        redisTemplate.boundHashOps(key).delete(String.valueOf(fid));
    }

    public void deleteFidToPidAndName(long uid, Object... ids) {
        var key = redisConfig.getMapFidToPidAndNameHashPrefix() + ":" + uid;
        redisTemplate.boundHashOps(key).delete(ids);
    }

    public String getPidAndName(long uid, long fid) {
        var key = redisConfig.getMapFidToPidAndNameHashPrefix() + ":" + uid;
        var pidAndName = redisTemplate.boundHashOps(key).get(String.valueOf(fid));
        if (pidAndName == null) return null;
        return pidAndName.toString();
    }
}
