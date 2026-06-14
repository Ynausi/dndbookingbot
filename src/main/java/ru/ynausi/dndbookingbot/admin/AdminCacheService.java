package ru.ynausi.dndbookingbot.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdminCacheService {
    private final GSAdminService gsAdminService;
    private final CacheManager cacheManager;
    private static final String ADMIN_CACHE_NAME = "admin";
    private static final String ADMIN_CACHE_KEY = "adminKey";

    @Cacheable(cacheNames = ADMIN_CACHE_NAME,key = "'" + ADMIN_CACHE_KEY + "'")
    public Optional<Admin> getAdmin() {
        return gsAdminService.getAdmin();
    }

    public boolean updateAdminTelegramUserIdAndChatId(Long telegramUserId, Long chatId) {
        boolean updated = gsAdminService.updateAdminTelegramIdAndChatId(telegramUserId,chatId);
        if (updated) {
            updatedCache(telegramUserId,chatId);
        }
        return updated;
    }

    public boolean updatePhoto(String fileId,String photoName) {
        boolean updated = gsAdminService.updatePhotoOnAdminList(fileId,photoName);
        if (updated) {
            updatedCache(fileId,photoName);
        }
        return updated;
    }

    private void updatedCache(String fileId,String photoName) {
        Cache cache = cacheManager.getCache(ADMIN_CACHE_NAME);
        if (cache == null) {
            return;
        }
        Optional<Admin> admin = cache.get(ADMIN_CACHE_KEY, Optional.class);
        if (admin.isEmpty()) {
            return;
        }
        if (photoName.equals(admin.get().getAdventurePhotoId())) {
            admin.get().setAdventurePhotoId(fileId);
        } else if (photoName.equals(admin.get().getOneShotPhotoId())) {
            admin.get().setCompanyPhotoId(fileId);
        } else {
            admin.get().setOneShotPhotoId(fileId);
        }
        cache.put(ADMIN_CACHE_KEY,admin);
    }

    private void updatedCache(Long telegramUserId, Long chatId) {
        Cache cache = cacheManager.getCache(ADMIN_CACHE_NAME);
        if (cache == null) {
            return;
        }
        Optional<Admin> admin = cache.get(ADMIN_CACHE_KEY, Optional.class);
        if (admin.isEmpty()) {
            return;
        }
        admin.get().setTelegramUserId(telegramUserId);
        admin.get().setChatId(chatId);
        cache.put(ADMIN_CACHE_KEY,admin);
    }

}
