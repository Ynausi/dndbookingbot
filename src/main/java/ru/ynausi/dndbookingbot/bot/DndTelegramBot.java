package ru.ynausi.dndbookingbot.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.ynausi.dndbookingbot.bot.update.UpdateDispatcher;
import ru.ynausi.dndbookingbot.configuration.TelegramBotProperties;

@Component
public class DndTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramBotProperties properties;
    private final UpdateDispatcher updateDispatcher;

    public DndTelegramBot(TelegramBotProperties properties, UpdateDispatcher updateDispatcher) {
        this.properties = properties;
        this.updateDispatcher = updateDispatcher;
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
        updateDispatcher.dispatch(update);
    }
}
