package ru.ynausi.dndbookingbot.bot.update;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.ynausi.dndbookingbot.booking.BookingModeDateOrSlot;
import ru.ynausi.dndbookingbot.booking.BookingSession;
import ru.ynausi.dndbookingbot.booking.BookingSessionServiceImpl;
import ru.ynausi.dndbookingbot.booking.Steps;
import ru.ynausi.dndbookingbot.bot.TelegramSender;
import ru.ynausi.dndbookingbot.bot.callback.CallbackData;
import ru.ynausi.dndbookingbot.bot.view.BookingTextFactory;
import ru.ynausi.dndbookingbot.bot.view.BookingView;
import ru.ynausi.dndbookingbot.bot.view.MainMenuView;
import ru.ynausi.dndbookingbot.bot.view.MasterView;
import ru.ynausi.dndbookingbot.master.Master;
import ru.ynausi.dndbookingbot.master.MasterKeyboardFactory;
import ru.ynausi.dndbookingbot.master.MasterService;
import ru.ynausi.dndbookingbot.master.MasterTextFactory;
import ru.ynausi.dndbookingbot.schedule.ScheduleService;
import ru.ynausi.dndbookingbot.schedule.Slot;
import ru.ynausi.dndbookingbot.session.AdminMasterSession;
import ru.ynausi.dndbookingbot.session.AdminMasterSessionServiceImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class CallbackQueryHandler {
    private final TelegramSender sender;
    private final BookingSessionServiceImpl bookingSessionService;
    private final MasterService masterService;
    private final AdminMasterSessionServiceImpl sessionService;
    private final MainMenuView mainMenuView;
    private final BookingView bookingView;
    private final MasterView masterView;
    private final ScheduleService scheduleService;
    private final BookingTextFactory bookingTextFactory;
    private final MasterTextFactory masterTextFactory;
    private final MasterKeyboardFactory masterKeyboardFactory;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public CallbackQueryHandler(TelegramSender sender, BookingSessionServiceImpl bookingSessionService,
                                MasterService masterService, AdminMasterSessionServiceImpl sessionService,
                                MainMenuView mainMenuView, BookingView bookingView,
                                MasterView masterView, ScheduleService scheduleService, BookingTextFactory bookingTextFactory, MasterTextFactory masterTextFactory, MasterKeyboardFactory masterKeyboardFactory) {
        this.sender = sender;
        this.bookingSessionService = bookingSessionService;
        this.masterService = masterService;
        this.sessionService = sessionService;
        this.mainMenuView = mainMenuView;
        this.bookingView = bookingView;
        this.masterView = masterView;
        this.scheduleService = scheduleService;
        this.bookingTextFactory = bookingTextFactory;
        this.masterTextFactory = masterTextFactory;
        this.masterKeyboardFactory = masterKeyboardFactory;
    }

    public void handle(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long telegramUserId = callbackQuery.getFrom().getId();
        String userName = callbackQuery.getFrom().getUserName();

        if (data.startsWith(CallbackData.CHOSEN_MASTER_PREFIX)) {
            handleChosenMaster(callbackQuery,chatId,telegramUserId,data);
            return;
        }
        if (data.startsWith(CallbackData.SLOT_PREFIX)) {
            handleSlot(callbackQuery,data);
            return;
        }
        if (data.startsWith(CallbackData.DATE_PREFIX)) {
            handleDate(callbackQuery,data);
            return;
        }
        if (data.startsWith(CallbackData.CONFIRMATION)) {
            handleConfirmation(callbackQuery,data);
            sender.removeInlineKeyBoard(callbackQuery);
            return;
        }
        if (data.startsWith(CallbackData.PHOTO_PREFIX)) {
            handlePhotoSettings(chatId,telegramUserId,data);
            return;
        }
        if(data.startsWith(CallbackData.DATE_OR_TIME_MODE)) {
            handleBookingMode(callbackQuery,data);
            return;
        }
        if (data.startsWith(CallbackData.MASTER_PAGE_PREFIX)) {
            handleMasterPage(callbackQuery, data);
            return;
        }
        if (data.startsWith(CallbackData.MASTER_VIEW_PREFIX)) {
            handleMasterView(callbackQuery,data);
            return;
        }
        if (data.startsWith(CallbackData.MASTER_BACK_TO_LIST)) {
            handleMasterBackToList(callbackQuery,data);
            return;
        }
        switch (data) {
            case CallbackData.COMPANY -> sender.sendMessage(chatId,
                    "Компейн бронируется по согласованию с мастером.\n" +
                    "Уже отправили ему вашу ссылку. Скоро он с вами свяжется");
            case CallbackData.BOOKING -> {
                if (bookingSessionService.findByTelegramUserId(telegramUserId).isEmpty())  {
                    bookingSessionService.startSession(chatId,telegramUserId,userName);
                }
                bookingView.showBookingDates(chatId,telegramUserId);
            }
            case CallbackData.ONE_SHOT -> {
                mainMenuView.showStartMenu(chatId);
                sender.removeInlineKeyBoard(callbackQuery);
            }
            case CallbackData.MASTERS -> {
                if (bookingSessionService.findByTelegramUserId(telegramUserId).isEmpty()) {
                    bookingSessionService.startSession(chatId,telegramUserId,userName);
                }
                Optional<BookingSession> bS = bookingSessionService.findByTelegramUserId(telegramUserId);
                Optional<Integer> messageId =  masterView.showMasters(chatId);
                messageId.ifPresent(id->bS.get().setMasterListMessageId(id));
            }
            default -> sender.sendMessage(chatId,"Неизвестная команда");
        }
    }

    private void handleMasterBackToList(CallbackQuery callbackQuery, String data) {
        Integer page = Integer.parseInt(data.substring(CallbackData.MASTER_BACK_TO_LIST.length()));
        Long chatId = callbackQuery.getMessage().getChatId();
        sender.deleteMessage(callbackQuery);
    }

    private void handleMasterView(CallbackQuery callbackQuery, String data) {
        String[] dataInf = (data.substring(CallbackData.MASTER_VIEW_PREFIX.length())).split(":");
        String masterCode = dataInf[0];
        Long telegramUserId = callbackQuery.getFrom().getId();
        Long chatId = callbackQuery.getMessage().getChatId();
        Optional<BookingSession> bS = bookingSessionService.findByTelegramUserId(telegramUserId);
        if (bS.isEmpty()) {
            sender.sendMessage(chatId,"Что-то не так с бронированием, попробуйте снова");
            return;
        }
        Optional<Master> master = masterService.findByMasterCode(masterCode);
        int page = Integer.parseInt(dataInf[1]);
        Optional<Integer> messageId = masterView.showMasterCard(master.get(),chatId,page);
        messageId.ifPresent(id->bS.get().setMasterViewMessageId(id));
    }

    private void handleMasterPage(CallbackQuery callbackQuery, String data) {
        int page = Integer.parseInt(data.substring(CallbackData.MASTER_PAGE_PREFIX.length()));
        List<Master> masters = masterService.getActiveMasters();
        String text = masterTextFactory.buildMasterPageText(masters, page);
        InlineKeyboardMarkup keyboard = masterKeyboardFactory.buildMasterPageKeyboard(masters, page);
        sender.editTextMessage(callbackQuery, text, keyboard);
    }

    private void handleBookingMode(CallbackQuery callbackQuery,String data) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Long telegramUserId = callbackQuery.getFrom().getId();
        String bookingMode = data.substring(CallbackData.DATE_OR_TIME_MODE.length());
        Optional<BookingSession> bS = bookingSessionService.findByTelegramUserId(telegramUserId);
        if (bS.isEmpty()) {
            sender.sendMessage(chatId,"Что-то не так с бронированием, попробуйте снова");
            return;
        }

        if ("date".equals(bookingMode)) {
            bS.get().setBookingModeDateOrSlot(BookingModeDateOrSlot.BY_DATE);
            bS.get().setStep(Steps.DATE);
            Optional<InlineKeyboardMarkup> keyboard = bookingView.showFreeBookingDatesForMaster(chatId,telegramUserId);
            if (keyboard.isEmpty()) {
                sender.sendMessage(chatId,"У мастера нет свободных мест");
                return;
            }
            String text = bookingTextFactory.buildMasterText(bS.get());
            sender.editTextMessage(callbackQuery,text,keyboard.get());
        } else if ("slot".equals(bookingMode)) {
            bS.get().setBookingModeDateOrSlot(BookingModeDateOrSlot.BY_SLOT);
            bS.get().setStep(Steps.SLOT);
            InlineKeyboardMarkup keyboard = bookingView.showBookingTime(chatId);
            String text = bookingTextFactory.buildMasterText(bS.get());
            sender.editTextMessage(callbackQuery,text,keyboard);
        }
        else {
            sender.sendMessage(chatId,"Извините, что-то пошло не такHandleBookingMode");
        }
    }

    private void handleChosenMaster(CallbackQuery callbackQuery,Long chatId,Long telegramUserId,String data) {
        String masterCode = data.substring(CallbackData.CHOSEN_MASTER_PREFIX.length()).trim();
        Optional<Master> master = masterService.findByMasterCode(masterCode);
        if (master.isEmpty()) {
            sender.sendMessage(chatId,"Извините, что-то пошло не так");
            return;
        }
        Optional<BookingSession> bS = bookingSessionService.findByTelegramUserId(telegramUserId);
        if (bS.isEmpty()) {
            sender.sendMessage(chatId,"Извините, что-то пошло не так");
            return;
        }
        sender.removeInlineKeyboard(chatId, bS.get().getMasterListMessageId());
        sender.removeInlineKeyboard(chatId,bS.get().getMasterViewMessageId());
        bS.get().setSelectedMasterCode(masterCode);
        bS.get().setStep(Steps.BOOKING_MODE);
        String text = bookingTextFactory.buildMasterText(bS.get());
        InlineKeyboardMarkup keyboard = bookingView.showBookingDateOrTime(chatId);
        sender.sendMessage(chatId,text,keyboard);
    }

    private void handleSlot(CallbackQuery callbackQuery,String data) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Long telegramUserId = callbackQuery.getFrom().getId();
        String selectedSlot = data.substring(CallbackData.SLOT_PREFIX.length());
        boolean saved = bookingSessionService.selectSlot(telegramUserId, Slot.valueOf(selectedSlot));

        if (!saved) {
            sender.sendMessage(chatId, "Бронь устарела. Начните заново");
            return;
        }
        Optional<BookingSession> bS = bookingSessionService.findByTelegramUserId(telegramUserId);
        if (bS.isEmpty()) {
            sender.sendMessage(chatId,"Что-то не так с бронью. Начните заново");
            return;
        }
        if (bS.get().getBookingModeDateOrSlot()==BookingModeDateOrSlot.BY_SLOT) {
            Optional<InlineKeyboardMarkup> keyboard = bookingView.showBookingDates(chatId,telegramUserId);
            if (keyboard.isEmpty()) {
                sender.sendMessage(chatId,"Видимо у мастера нет свободных мест на месяц вперёд");
            }
            bS.get().setStep(Steps.DATE);
            String text = bookingTextFactory.buildMasterText(bS.get());
            sender.editTextMessage(callbackQuery,text,keyboard.get());
        }
        if (bS.get().getBookingModeDateOrSlot() == BookingModeDateOrSlot.BY_DATE) {
            InlineKeyboardMarkup keyboard = bookingView.showConfirm(chatId);
            bS.get().setStep(Steps.CONFIRM);
            String text = bookingTextFactory.buildMasterText(bS.get());
            sender.editTextMessage(callbackQuery,text,keyboard);
        }
    }

    private void handleDate(CallbackQuery callbackQuery,String data) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Long telegramUserId = callbackQuery.getFrom().getId();
        String selectedDate = data.substring(CallbackData.DATE_PREFIX.length());
        boolean saved = bookingSessionService.selectDate(telegramUserId, LocalDate.parse(selectedDate,DATE_FORMATTER));

        if (!saved) {
            sender.sendMessage(chatId,"Бронирование устарело. Начните заново");
            return;
        }
        Optional<BookingSession> bS = bookingSessionService.findByTelegramUserId(telegramUserId);
        if (bS.isEmpty()) {
            sender.sendMessage(chatId,"Что-то не так с бронированием. Начните заново");
            return;
        }
        if (bS.get().getBookingModeDateOrSlot() == BookingModeDateOrSlot.BY_SLOT) {
            bS.get().setStep(Steps.CONFIRM);
            InlineKeyboardMarkup keyboard = bookingView.showConfirm(chatId);
            String text = bookingTextFactory.buildMasterText(bS.get());
            sender.editTextMessage(callbackQuery,text,keyboard);
        }
        if (bS.get().getBookingModeDateOrSlot() == BookingModeDateOrSlot.BY_DATE) {
            bS.get().setStep(Steps.SLOT);
            Optional<InlineKeyboardMarkup> keyboard =
                    bookingView.showFreeBookingSlotForMasterByDate(chatId,
                            bS.get().getSelectedMasterCode(),
                            bS.get().getSelectedDate());
            if (keyboard.isEmpty()) {
                sender.sendMessage(chatId,"Видимо у мастера нет свободных мест");
            }
            String text = bookingTextFactory.buildMasterText(bS.get());
            sender.editTextMessage(callbackQuery,text,keyboard.get());
        }
    }

    private void handleConfirmation(CallbackQuery callbackQuery,String data) {
        Long chatId = callbackQuery.getMessage().getChatId();
        Long telegramUserId = callbackQuery.getFrom().getId();
        Optional<BookingSession> bookingSession = bookingSessionService.findByTelegramUserId(telegramUserId);
        if (bookingSession.isEmpty()) {
            sender.sendMessage(chatId,"Извините возника проблема с бронированием, попробуйте снова");
        }
        Optional<Master> master = masterService.findByMasterCode(bookingSession.get().getSelectedMasterCode());
        if (master.isEmpty()) {
            sender.sendMessage(chatId,"Не удалось найти мастера, попробуйте снова");
            return;
        }

        BookingSession session = bookingSession.get();
        Master selectedMaster = master.get();
        Slot selectedSlot = (session.getSelectedSlot());
        String time  = selectedSlot == Slot.FIRST
                ? "12:00-17:00"
                : "18:00-23:00";


        scheduleService.updateMasterSchedule(selectedMaster.masterCode(),session.getSelectedDate(),
                session.getSelectedSlot(), session.getUserName()
        );
        sender.sendMessage(chatId,"Вы успешно записались на "
                + time
                + " "
                + session.getSelectedDate()
                + ". \n"
                + "Ждём вас!"
        );
        sender.sendMessage(Long.valueOf(master.get().chatId()),
                "У вас забронировали игру "
                        + session.getSelectedDate()
                        + " "
                        + time
                        + " @" + session.getUserName()
        );
    }

    private void handlePhotoSettings(Long chatId,Long telegramUserId,String data) {
        String selectedPhoto = data.substring(CallbackData.PHOTO_PREFIX.length());
        Optional<AdminMasterSession> session = sessionService.findSessionByTelegramUserId(telegramUserId);
        if (session.isEmpty()) {
            sender.sendMessage(chatId,"Пожалуйста сначала зарегиструйтесь");
            return;
        }
        session.get().setStep(selectedPhoto);
        sender.sendMessage(chatId,"Отправьте фото которое вы хотите установить");
    }

    private String formatSlot(Slot slot) {
        if (slot == Slot.FIRST) {
            return "12:00-17:00";
        }
        return "18:00-23:00";
    }
}
