package ru.ynausi.dndbookingbot.bot.view;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.ynausi.dndbookingbot.bot.TelegramSender;
import ru.ynausi.dndbookingbot.bot.callback.CallbackData;

import java.util.List;


@Component
public class AdminSettingsView {
    private final TelegramSender sender;


    public AdminSettingsView(TelegramSender sender) {
        this.sender = sender;
    }

    public void showSettingsMenu(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button("Изменить картинку главного меню", CallbackData.photo("adventure"))),
                new InlineKeyboardRow(button("Изменить картинку для ваншота",CallbackData.photo(CallbackData.photo("oneshot")))),
                new InlineKeyboardRow(button("Изменить картинку для компейна",CallbackData.photo("company")))
        ));
        sender.sendMessage(chatId,"Выбери какую картинку поменять",markup);
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
