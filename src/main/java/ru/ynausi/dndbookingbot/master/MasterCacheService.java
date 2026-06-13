package ru.ynausi.dndbookingbot.master;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MasterCacheService {
    private final GSMasterService gsMasterService;
    private final CacheManager cacheManager;
    private static final String MASTER_CACHE_NAME = "masters";
    private static final String MASTER_CACHE_KEY = "allMasters";

    @Cacheable(cacheNames = MASTER_CACHE_NAME,key = "'"+MASTER_CACHE_KEY+"'")
    public List<Master> getMasters() {
        log.info("Читаю лист мастеров из Google Sheets");
        return gsMasterService.getMastersInfo();
    }

    public boolean updateMasterTelegramIdAndChatId(Long telegramUserId, Long chatId, Master master) {
        boolean updated = gsMasterService.updateMasterTelegramIdAndChatId(telegramUserId,chatId,master);
        if (updated) {
            updatedMasterCache(telegramUserId,chatId,master);
        }
        return updated;
    }

    public boolean updateMasterPhoto(String fileId, Master master) {
        boolean updated = gsMasterService.updateMasterPhoto(fileId,master);
        if (updated) {
            updatedMasterCache(fileId,master);
        }
        return updated;
    }

    private boolean updatedMasterCache(Long telegramUserId,Long chatId,Master master) {
        Cache cache = cacheManager.getCache(MASTER_CACHE_NAME);
        if (cache == null) {
            return false;
        }
        List<Master> masters= cache.get(MASTER_CACHE_KEY, List.class);
        if (masters == null) {
            return false;
        }
        Optional<Master> oldMaster = masters.stream()
                .filter(master1-> master.masterCode().equals(master1.masterCode()))
                .findFirst();
        if (oldMaster.isEmpty()) {
            return false;
        }
        Master newMaster = oldMaster.get().toBuilder()
                .telegramUserId(telegramUserId)
                .chatId(chatId)
                .build();
        List<Master> updatedMasters = masters.stream()
                .map(currentMaster -> {
                    if (oldMaster.get().masterCode().equals(currentMaster.masterCode())) {
                        return newMaster;
                    }
                    return currentMaster;
                })
                .toList();
        cache.put(MASTER_CACHE_KEY,updatedMasters);
        return true;
    }

    private boolean updatedMasterCache(String photoFileId,Master master) {
        Cache cache = cacheManager.getCache(MASTER_CACHE_NAME);
        if (cache == null) {
            return false;
        }
        List<Master> masters= cache.get(MASTER_CACHE_KEY, List.class);
        if (masters == null) {
            return false;
        }
        Optional<Master> oldMaster = masters.stream().filter(master1-> master.masterCode().equals(master1.masterCode())).findFirst();
        if (oldMaster.isEmpty()) {
            return false;
        }
        Master newMaster = oldMaster.get().toBuilder()
                .photoFileId(photoFileId)
                .build();
        List<Master> updatedMasters = masters.stream()
                .map(currentMaster -> {
                    if (oldMaster.get().masterCode().equals(currentMaster.masterCode())) {
                        return newMaster;
                    }
                    return currentMaster;
                })
                .toList();
        cache.put(MASTER_CACHE_KEY,updatedMasters);
        return true;
    }
}
