package org.dreamcat.maid.cassandra.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.util.RandomUtil;
import org.springframework.stereotype.Service;

import java.util.Comparator;

/**
 * Create by tuke on 2020/4/12
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class InstanceService {
    private final InstanceDao instanceDao;
    private final InstanceFileDao instanceFileDao;

    public String findMostIdleAddress(String digest) {
        var addresses = instanceFileDao.findAddressesByDigest(digest);

        if (!addresses.isEmpty()) {
            return addresses.get(RandomUtil.randi(addresses.size()));
        }
        return null;
    }

    public String findMostIdleDomainAddress(String digest) {
        var address = findMostIdleAddress(digest);
        if (address == null) return null;
        var instance = instanceDao.findById(address).orElse(null);
        if (instance == null) {
            log.warn("Cannot find instance {} on table instance", address);
            return null;
        }
        return instance.getDomain();
    }

    public String findFreestAddress() {
        return instanceDao.findAll().stream()
                .min(Comparator.comparingLong(InstanceEntity::getFree))
                .map(InstanceEntity::getAddress)
                .orElse(null);
    }

    public String mapAddressToDomain(String address) {
        if (address == null) return null;
        return instanceDao.findById(address)
                .map(InstanceEntity::getDomain)
                .orElse(null);
    }
}
