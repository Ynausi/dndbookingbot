package ru.ynausi.dndbookingbot.bot.update;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.ynausi.dndbookingbot.admin.AdminService;
import ru.ynausi.dndbookingbot.bot.TelegramSender;
import ru.ynausi.dndbookingbot.bot.command.BotCommand;
import ru.ynausi.dndbookingbot.bot.view.AdminSettingsView;
import ru.ynausi.dndbookingbot.bot.view.MainMenuView;
import ru.ynausi.dndbookingbot.master.Master;
import ru.ynausi.dndbookingbot.master.MasterService;
import ru.ynausi.dndbookingbot.session.AdminMasterSessionServiceImpl;

import java.util.List;
import java.util.Optional;

@Component
public class TextMessageHandler {
    private static final int FIRST_MASTER_ROW_NUMBER =2;

    private final TelegramSender sender;
    private final MainMenuView mainMenuView;
    private final AdminSettingsView adminSettingsView;
    private final AdminService adminService;
    private final MasterService masterService;
    private final AdminMasterSessionServiceImpl sessionService;

    public TextMessageHandler(TelegramSender sender, MainMenuView mainMenuView, AdminSettingsView adminSettingsView, AdminService adminService, MasterService masterService, AdminMasterSessionServiceImpl sessionService) {
        this.sender = sender;
        this.mainMenuView = mainMenuView;
        this.adminSettingsView = adminSettingsView;
        this.adminService = adminService;
        this.masterService = masterService;
        this.sessionService = sessionService;
    }

    public void handle(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();

        if(BotCommand.START.equals(text)) {
            mainMenuView.showFirstMenu(chatId);
            return;
        }
        if (text.startsWith(BotCommand.ADMIN)) {
            handleAdminCommand(message,text);
            return;
        }
        if(text.startsWith(BotCommand.MASTER)) {
            handleMasterCommand(message,text);
        }
    }

    private void handleAdminCommand(Message message,String text) {
        Long chatId = message.getChatId();
        Long telegramUserId = message.getFrom().getId();
        String adminCode = text.substring(BotCommand.ADMIN.length()).trim();

        boolean isAdmin = adminService.checkIfAdmin(adminCode);
        if (!isAdmin) {
            sender.sendMessage(chatId,"Введите верный код админа");
        }
        adminService.updateAdminTelegramUserIdAndChatId(telegramUserId,chatId);
        sessionService.startSession(telegramUserId,"admin");
        adminSettingsView.showSettingsMenu(chatId);
    }

    private void handleMasterCommand(Message message,String text) {
        Long chatId = message.getChatId();
        Long telegramUserId = message.getFrom().getId();
        String masterCode = text.substring(BotCommand.MASTER.length()).trim();

        if (masterCode.isBlank()) {
            sender.sendMessage(chatId,"После /master укажите свой код");
            return;
        }

        List<Master> masters = masterService.getMasters();
        Optional<Master> master = masterService.findByMasterCode(masterCode);
        if (master.isEmpty()) {
            sender.sendMessage(chatId,"Не удалось найти этого мастера. Попробуйте выбрать другого");
            return;
        }
        sessionService.startSession(telegramUserId,"master");
        masterService.updateMasterTelegramIdAndChatId(telegramUserId,chatId,master.get());
        sender.sendMessage(chatId,"Отлично, ваши данные были обновлены. Теперь отправьте своё фото");
    }
}
