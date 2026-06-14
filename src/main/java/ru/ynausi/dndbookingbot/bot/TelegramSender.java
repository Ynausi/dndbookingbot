package ru.ynausi.dndbookingbot.bot;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
public class TelegramSender {
    private final TelegramClient telegramClient;

    public TelegramSender(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void sendMessage(Long chatId,String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        execute(message);
    }

    public void sendMessage(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(markup)
                .build();
        execute(message);
    }

    public void sendPhoto(Long chatId,String photoFileId,String caption) {
        SendPhoto photo = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(photoFileId))
                .caption(caption)
                .build();
        execute(photo);
    }

    public void answerCallbackQuery(CallbackQuery callbackQuery) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQuery.getId())
                .build();
        execute(answer);
    }

    public void sendPhoto(Long chatId,String photoFileId,String caption,InlineKeyboardMarkup markup) {
        SendPhoto photo = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(photoFileId))
                .caption(caption)
                .replyMarkup(markup)
                .build();
        execute(photo);
    }

    public Message sendPhotoAndGetMessageId(Long chatId,String photoFileId,String caption,InlineKeyboardMarkup markup) {
        SendPhoto photo = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(photoFileId))
                .caption(caption)
                .replyMarkup(markup)
                .build();
        return executeAndGetId(photo);
    }

    public void editTextMessage(CallbackQuery callbackQuery,String text,InlineKeyboardMarkup replyMarkup) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        EditMessageText editMessageText = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text)
                .replyMarkup(replyMarkup)
                .build();
        execute(editMessageText);
    }

    public void removeInlineKeyboard(Long chatId, Integer messageId) {
        EditMessageReplyMarkup editMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(null)
                .build();

        execute(editMarkup);
    }

    public @Nullable Message sendMessageAndGetMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();
        return executeAndGetId(sendMessage);
    }
    public void deleteMessage(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
        execute(deleteMessage);
    }

    public void deleteMessageById(Long chatId,Integer messageId){
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
        execute(deleteMessage);
    }

    private void execute(AnswerCallbackQuery answer) {
        try {
            telegramClient.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Не удалось ответить на callbackQuery id={}", answer.getCallbackQueryId(), e);
        }
    }

    private  @Nullable Message executeAndGetId(SendMessage message) {
        try {
            return telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в chatId={}",message.getChatId());
            return null;
        }
    }

    private  @Nullable Message executeAndGetId(SendPhoto photo) {
        try {
            return telegramClient.execute(photo);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в chatId={}",photo.getChatId());
            return null;
        }
    }

    private void execute(EditMessageText editMessageText) {
        try {
            telegramClient.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Не удалось обновить сообщение в chatId={}",editMessageText.getChatId());
        }
    }

    public void removeInlineKeyBoard(CallbackQuery callbackQuery){
        Long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        EditMessageReplyMarkup editMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(null)
                .build();
        execute(editMarkup);
    }

    private void execute(DeleteMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в chatId={}",message.getChatId());
        }
    }

    private void execute(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в chatId={}",message.getChatId());
        }
    }

    private void execute(EditMessageReplyMarkup editMarkup) {
        try {
            telegramClient.execute(editMarkup);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение в chatId={}",editMarkup.getChatId());
        }
    }

    private void execute(SendPhoto photo) {
        try {
            telegramClient.execute(photo);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить фото chatId={}",photo.getChatId());
        }
    }
}
