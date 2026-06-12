package ru.ynausi.dndbookingbot.bot.view;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.ynausi.dndbookingbot.admin.Admin;
import ru.ynausi.dndbookingbot.admin.AdminService;
import ru.ynausi.dndbookingbot.bot.TelegramSender;
import ru.ynausi.dndbookingbot.bot.callback.CallbackData;

import java.util.List;
import java.util.Optional;

@Component
public class MainMenuView {
    private static final String FIRST_MENU_TEXT = "Привет, приключенцы! Мы – интерактивный театр «Врата Героев». " +
            "В нашем пространстве мы проводим настольно-ролевые игры. " +
            "Хотите отправиться в приключение? Наши ведущие создадут невероятную атмосферу для вас и ваших друзей. " +
            "Вы станете главными героями истории, разворачивающейся в мирах Dungeons and Dragons, " +
            "Vampires: The Masquerade, Pathfinder и других!\n" +
            "Начать приключение!";
    private static final String MASTER_BUTTON = "🧙‍♂️ Выбор мастера";
    private static final String DATE_BUTTON = "🕓 Выбор даты";
    private static final String MAIN_BUTTON = "🎲 С чего начём, путешественники?";

    private final TelegramSender sender;
    private final AdminService adminService;

    public MainMenuView(TelegramSender sender, AdminService adminService) {
        this.sender = sender;
        this.adminService = adminService;
    }

    public void showFirstMenu(Long chatId) {
        InlineKeyboardMarkup markup = createFirstMenuKeyboard();
        Optional<Admin> admin = adminService.getAdmin();
        if (admin.get().getAdventurePhotoId().isEmpty()) {
            sender.sendMessage(chatId,FIRST_MENU_TEXT,markup);
            return;
        }
        String photoId = admin.get().getAdventurePhotoId();
        sender.sendPhoto(chatId,photoId,FIRST_MENU_TEXT,markup);
    }

    public void showStartMenu(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(MASTER_BUTTON,CallbackData.MASTERS)),
                new InlineKeyboardRow(button(DATE_BUTTON,CallbackData.BOOKING))
        ));
        sender.sendMessage(chatId,MAIN_BUTTON,markup);
    }

    private InlineKeyboardMarkup createFirstMenuKeyboard() {
        return new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button("Забронировать ваншот (приключение на один вечер)", CallbackData.ONE_SHOT)),
                new InlineKeyboardRow(button("Забронировать компейн (долгое приключение)", CallbackData.COMPANY))
        ));
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
