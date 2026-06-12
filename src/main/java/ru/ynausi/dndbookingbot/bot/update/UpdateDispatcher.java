package ru.ynausi.dndbookingbot.bot.update;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateDispatcher {
    private final CallbackQueryHandler callbackQueryHandler;
    private final PhotoMessageHandler photoMessageHandler;
    private final TextMessageHandler textMessageHandler;

    public UpdateDispatcher(CallbackQueryHandler callbackQueryHandler, PhotoMessageHandler photoMessageHandler, TextMessageHandler textMessageHandler) {
        this.callbackQueryHandler = callbackQueryHandler;
        this.photoMessageHandler = photoMessageHandler;
        this.textMessageHandler = textMessageHandler;
    }

    public void dispatch(Update update) {
        if (update.hasCallbackQuery()) {
            callbackQueryHandler.handle(update.getCallbackQuery());
        }
        if(update.hasMessage()&&update.getMessage().hasPhoto()) {
            photoMessageHandler.handle(update.getMessage());
        }
        if(update.hasMessage()&&update.getMessage().hasText()) {
            textMessageHandler.handle(update.getMessage());
        }
    }
}
