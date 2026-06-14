package ru.ynausi.dndbookingbot.admin;

import java.util.Optional;

public interface AdminService {

    Optional<Admin> getAdmin();

    void updateAdminTelegramUserIdAndChatId(Long telegramUserId,Long chatId);

    boolean checkIfAdmin(String adminCode);

    void updatePhoto(Long telegramUserId,String fileId,String photoName);

}
