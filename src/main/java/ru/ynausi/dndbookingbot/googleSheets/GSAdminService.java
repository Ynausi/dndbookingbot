package ru.ynausi.dndbookingbot.googleSheets;

import ru.ynausi.dndbookingbot.admin.Admin;

import java.util.List;
import java.util.Optional;

public interface GSAdminService {
    void updateAdminTelegramIdAndChatId(Long telegramUserId,Long chatId);

    void updatePhotoOnAdminList(Long telegramId,String fileId,String photoName);

    boolean checkIfAdmin(String adminCode);

    List<List<Object>> getAdminRows();

    Optional<Admin> getAdmin();
}
