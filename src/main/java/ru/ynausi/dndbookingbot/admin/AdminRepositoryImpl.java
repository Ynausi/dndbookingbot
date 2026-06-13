package ru.ynausi.dndbookingbot.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
//import ru.ynausi.dndbookingbot.googleSheets.GoogleSheetsService;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@CacheConfig(cacheNames = "admin")
public class AdminRepositoryImpl implements AdminRepository{
    private final GSAdminService gsAdminService;

    @Override
    @Cacheable
    public Optional<Admin> getAdmin() {
        return (gsAdminService.getAdmin());
    }

    @Override
    public void updatePhoto(Long telegramUserId, String fileId,String photoName) {
        gsAdminService.updatePhotoOnAdminList(telegramUserId,fileId,photoName);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void updateAdminTelegramUserIdAndChatId(Long telegramUserId, Long chatId) {
        gsAdminService.updateAdminTelegramIdAndChatId(telegramUserId,chatId);
    }

    @Override
    public boolean checkIfAdmin(String adminCode) {
        return gsAdminService.checkIfAdmin(adminCode);
    }
}
