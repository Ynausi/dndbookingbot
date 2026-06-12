package ru.ynausi.dndbookingbot.bot.update;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;
import ru.ynausi.dndbookingbot.admin.AdminService;
import ru.ynausi.dndbookingbot.bot.TelegramSender;
import ru.ynausi.dndbookingbot.master.Master;
import ru.ynausi.dndbookingbot.master.MasterService;
import ru.ynausi.dndbookingbot.session.AdminMasterSession;
import ru.ynausi.dndbookingbot.session.AdminMasterSessionServiceImpl;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class PhotoMessageHandler {
    private static final String ROLE_MASTER = "master";
    private static final String ROLE_ADMIN = "admin";

    private final TelegramSender sender;
    private final MasterService masterService;
    private final AdminService adminService;
    private final AdminMasterSessionServiceImpl sessionService;

    public PhotoMessageHandler(TelegramSender sender, MasterService masterService, AdminService adminService, AdminMasterSessionServiceImpl sessionService) {
        this.sender = sender;
        this.masterService = masterService;
        this.adminService = adminService;
        this.sessionService = sessionService;
    }

    public void handle(Message message) {
        Long chatId = message.getChatId();
        Long telegramUserId = message.getFrom().getId();
        String fileId = getLargestPhotoFileId(message.getPhoto());

        Optional<AdminMasterSession> session = sessionService.findSessionByTelegramUserId(telegramUserId);
        if (session.isEmpty()) {
            sender.sendMessage(chatId,"Извините, пока не могу работать с картинками от пользователей");
            return;
        }
        String role = session.get().getRole();
        if (ROLE_ADMIN.equals(role)) {
            updateAdminPhoto(chatId,telegramUserId,fileId,session.get());
            return;
        }
        if(ROLE_MASTER.equals(role)) {
            updateMasterPhoto(chatId,telegramUserId,fileId);
            return;
        }
        sender.sendMessage(chatId,"Извините, пока не могу работать с картинками от пользователей");
    }

    private void updateMasterPhoto(Long chatId,Long telegramUserId,String fileId) {
        Optional<Master> master = masterService.findMasterByTelegramId(telegramUserId);
        if (master.isEmpty()) {
            sender.sendMessage(chatId,"Что-то пошло не так. Проверьте свой код мастера и начните снова");
            return;
        }
        masterService.updateMasterPhoto(fileId,master.get());
        sender.sendMessage(chatId,"Ваше фото было успешно обновлено");
    }

    private void updateAdminPhoto(Long chatId,Long telegramUserId,String fileId,AdminMasterSession session) {
        adminService.updatePhoto(telegramUserId,fileId,session.getStep());
        sender.sendMessage(chatId,"Фото успешно обновлено");
    }

    private String getLargestPhotoFileId(List<PhotoSize> photos) {
        PhotoSize largestPhoto = photos.get(photos.size() - 1);
        return largestPhoto.getFileId();
    }
}
