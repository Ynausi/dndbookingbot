package ru.ynausi.dndbookingbot.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ynausi.dndbookingbot.googleSheets.GoogleSheetsService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{
    private final AdminRepository adminRepository;

    @Override
    public Optional<Admin> getAdmin() {
        return adminRepository.getAdmin();
    }

    @Override
    public void updateAdminTelegramUserIdAndChatId(Long telegramUserId,Long chatId) {
        adminRepository.updateAdminTelegramUserIdAndChatId(telegramUserId,chatId);
    }

    @Override
    public boolean checkIfAdmin(String adminCode) {
        return adminRepository.checkIfAdmin(adminCode);
    }

    @Override
    public void updatePhoto(Long telegramUserId,String fileId,String photoName) {
        adminRepository.updatePhoto(telegramUserId,fileId,photoName);
    }


}
