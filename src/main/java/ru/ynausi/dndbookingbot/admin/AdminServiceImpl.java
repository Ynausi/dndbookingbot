package ru.ynausi.dndbookingbot.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{
    private final AdminCacheService adminCache;

    @Override
    public Optional<Admin> getAdmin() {
        return adminCache.getAdmin();
    }

    @Override
    public void updateAdminTelegramUserIdAndChatId(Long telegramUserId,Long chatId) {
        adminCache.updateAdminTelegramUserIdAndChatId(telegramUserId,chatId);
    }

    @Override
    public boolean checkIfAdmin(String adminCode) {
        Optional<Admin> admin = adminCache.getAdmin();
        return admin.filter(value -> adminCode.equals(value.getAdminCode())).isPresent();
    }

    @Override
    public void updatePhoto(Long telegramUserId,String fileId,String photoName) {
        adminCache.updatePhoto(fileId,photoName);
    }


}
