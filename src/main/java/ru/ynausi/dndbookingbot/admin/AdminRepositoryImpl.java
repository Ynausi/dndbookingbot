package ru.ynausi.dndbookingbot.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import ru.ynausi.dndbookingbot.googleSheets.GoogleSheetsService;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@CacheConfig(cacheNames = "admin")
public class AdminRepositoryImpl implements AdminRepository{
    private final GoogleSheetsService googleSheetsService;

    @Override
    @Cacheable
    public Optional<Admin> getAdmin() {
        return (googleSheetsService.getAdmin());
    }

    @Override
    public void updatePhoto(Long telegramUserId, String fileId,String photoName) {
        googleSheetsService.updatePhotoOnAdminList(telegramUserId,fileId,photoName);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void updateAdminTelegramUserIdAndChatId(Long telegramUserId, Long chatId) {
        googleSheetsService.updateAdminTelegramIdAndChatId(telegramUserId,chatId);
    }

    @Override
    public boolean checkIfAdmin(String adminCode) {
        return googleSheetsService.checkIfAdmin(adminCode);
    }
}
