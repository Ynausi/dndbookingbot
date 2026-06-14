package ru.ynausi.dndbookingbot.admin;

import java.util.Optional;

public interface GSAdminService {
    boolean updateAdminTelegramIdAndChatId(Long telegramUserId,Long chatId);

    boolean updatePhotoOnAdminList(String fileId,String photoName);

    Optional<Admin> getAdmin();
}
