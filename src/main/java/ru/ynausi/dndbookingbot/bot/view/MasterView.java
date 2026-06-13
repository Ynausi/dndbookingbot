package ru.ynausi.dndbookingbot.bot.view;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.ynausi.dndbookingbot.bot.TelegramSender;
import ru.ynausi.dndbookingbot.bot.callback.CallbackData;
import ru.ynausi.dndbookingbot.master.Master;
import ru.ynausi.dndbookingbot.master.MasterKeyboardFactory;
import ru.ynausi.dndbookingbot.master.MasterService;
import ru.ynausi.dndbookingbot.master.MasterTextFactory;
import ru.ynausi.dndbookingbot.schedule.ScheduleService;
import ru.ynausi.dndbookingbot.schedule.Slot;

import java.time.LocalDate;
import java.util.*;

@Component
public class MasterView {
    private final TelegramSender sender;
    private final MasterService masterService;
    private final MasterTextFactory masterTextFactory;
    private final MasterKeyboardFactory masterKeyboardFactory;
    private final ScheduleService scheduleService;

    private static final String BACK_BUTTON ="⬅️";
    private static final String CONFIRM_BUTTON = "✅";
    public MasterView(TelegramSender sender, MasterService masterService, MasterTextFactory masterTextFactory, MasterKeyboardFactory masterKeyboardFactory, ScheduleService scheduleService) {
        this.sender = sender;
        this.masterService = masterService;
        this.masterTextFactory = masterTextFactory;
        this.masterKeyboardFactory = masterKeyboardFactory;
        this.scheduleService = scheduleService;
    }


    public Optional<Integer> showMasters(Long chatId) {
        List<Master> masters = masterService.getActiveMasters();
        if (masters.isEmpty()) {
            sender.sendMessage(chatId, "Пока нет доступных мастеров");
            return Optional.empty();
        }
        int page = 0;
        String text = masterTextFactory.buildMasterPageText(masters, page);
        InlineKeyboardMarkup keyboard = masterKeyboardFactory.buildMasterPageKeyboard(masters, page);
        Message message = sender.sendMessageAndGetMessage(chatId, text, keyboard);
        if(message == null) {
            return Optional.empty();
        }
        return Optional.of(message.getMessageId());
    }

    public Optional<Integer> showFreeMastersForDateAndSlot(Long chatId, LocalDate date, Slot slot) {
        List<Master> masters = scheduleService.findFreeMastersForDateAndSlot(date,slot);
        if (masters.isEmpty()) {
            sender.sendMessage(chatId, "Пока нет доступных мастеров");
            return Optional.empty();
        }
        int page = 0;
        String text = masterTextFactory.buildMasterPageText(masters, page);
        InlineKeyboardMarkup keyboard = masterKeyboardFactory.buildMasterPageKeyboard(masters, page);
        Message message = sender.sendMessageAndGetMessage(chatId, text, keyboard);
        if(message == null) {
            return Optional.empty();
        }
        return Optional.of(message.getMessageId());
    }

    public Optional<Integer> showMasterCard(Master master,Long chatId,Integer page) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(BACK_BUTTON,CallbackData.masterBackToList(page))),
                new InlineKeyboardRow(button(CONFIRM_BUTTON, CallbackData.chosenMaster(master.masterCode())))
        ));
        String caption = master.name() + "\n\n" + master.description();
        String photoFileId = master.photoFileId();
        if (photoFileId == null || photoFileId.isBlank()) {
           Message message = sender.sendMessageAndGetMessage(chatId,caption,markup);
            if(message == null) {
                return Optional.empty();
            }
           return Optional.of(message.getMessageId());
        }
        Message message = sender.sendPhotoAndGetMessageId(chatId,photoFileId,caption,markup);
        if(message == null) {
            return Optional.empty();
        }
        return Optional.of(message.getMessageId());
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}