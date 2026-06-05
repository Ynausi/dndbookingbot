package ru.ynausi.dndbookingbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.ynausi.dndbookingbot.configuration.TelegramBotProperties;

import java.util.List;

@Slf4j
@Component
public class DndTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramBotProperties properties;
    private final TelegramClient telegramClient;

    public DndTelegramBot(TelegramBotProperties properties) {
        this.properties = properties;
        this.telegramClient = new OkHttpTelegramClient(properties.token());
    }

    @Override
    public String getBotToken() {
        return properties.token();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {

        if (update.hasCallbackQuery()){
            handleCallbackQuery(update.getCallbackQuery());
            return;
        }

        if(!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if("/start".equals(text)) {
            sendStartMenu(chatId);
        }
    }

    private void sendStartMenu(Long chatId) {
        var button2 = InlineKeyboardButton.builder()
                .text("Выбор мастера")
                .callbackData("masters")
                .build();

        var button3 = InlineKeyboardButton.builder()
                .text("Выбор даты")
                .callbackData("booking")
                .build();
        List<InlineKeyboardRow> keyboardRows1 = List.of(
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows1);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Главное меню")
                .replyMarkup(markup)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        switch (data) {
            case "masters" -> sendMasters(chatId);
            case "booking" -> sendBookingDates(chatId);
            default -> sendMessage(chatId,"Неизвестная команда");
        }
    }

    private void sendBookingDates(Long chatId) {
    }

    private void sendStart(Long chatId) {

        var button1 = InlineKeyboardButton.builder()
                .text("Старт")
                .callbackData("start")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Добро пожаловать! Чтобы продолжить нажмите кнопку Старт")
                .replyMarkup(markup)
                .build();
        try{
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMasters(Long chatId) {

    }

    private void sendMessage(Long chatId,String text){
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try{
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
