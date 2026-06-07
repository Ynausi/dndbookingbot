package ru.ynausi.dndbookingbot.admin;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository {

    Optional<Admin> getAdmin();

    void updatePhoto(Long telegramUserId,String fileId,String photoName);

    void updateAdminTelegramUserIdAndChatId(Long telegramUserId,Long chatId);

    boolean checkIfAdmin(String adminCode);
}
