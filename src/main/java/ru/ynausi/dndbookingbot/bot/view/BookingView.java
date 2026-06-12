package ru.ynausi.dndbookingbot.bot.view;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.ynausi.dndbookingbot.booking.BookingSession;
import ru.ynausi.dndbookingbot.booking.BookingSessionServiceImpl;
import ru.ynausi.dndbookingbot.bot.TelegramSender;
import ru.ynausi.dndbookingbot.bot.callback.CallbackData;
import ru.ynausi.dndbookingbot.master.Master;
import ru.ynausi.dndbookingbot.master.MasterService;
import ru.ynausi.dndbookingbot.schedule.DaySchedule;
import ru.ynausi.dndbookingbot.schedule.ScheduleService;
import ru.ynausi.dndbookingbot.schedule.Slot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class BookingView {
    private final TelegramSender sender;
    private final BookingSessionServiceImpl bookingSessionService;
    private final MasterService masterService;
    private final ScheduleService scheduleService;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String DATE_BUTTON_TEXT = "📆 Дата";
    private static final String TIME_BUTTON_TEXT = "🕓 Время";
    public static final String CONFIRM = "✅";
    public static final String BACK = "⬅️";
    public static final String CANCEL = "❌";

    public BookingView(TelegramSender sender, BookingSessionServiceImpl bookingSessionService, MasterService masterService, ScheduleService scheduleService) {
        this.sender = sender;
        this.bookingSessionService = bookingSessionService;
        this.masterService = masterService;
        this.scheduleService = scheduleService;
    }

    public InlineKeyboardMarkup showBookingTime(Long chatId) {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button("12:00-17:00", CallbackData.slot(Slot.FIRST))),
                new InlineKeyboardRow(button("18:00-23:00",CallbackData.slot(Slot.SECOND)))
        ));
        return markup;
    }

    public Optional<InlineKeyboardMarkup> showBookingDates(Long chatId,Long telegramUserId) {
        Optional<BookingSession> bookingSession = bookingSessionService.findByTelegramUserId(telegramUserId);
        if (bookingSession.isEmpty()) {
            sender.sendMessage(chatId,"Проблемы с бронированием, попробуйте снова");
            return Optional.empty();
        }
        Optional<Master> master = masterService.findByMasterCode(bookingSession.get().getSelectedMasterCode());
        if (master.isEmpty()) {
            sender.sendMessage(chatId,"Мастер не найден. Попробуйте начать бронирование снова");
            return Optional.empty();
        }

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(1);
        Set<LocalDate> dates = new TreeSet<>();
        Map<LocalDate, DaySchedule> masterSchedule = scheduleService.getMasterSchedule(master.get().masterCode());
        Optional<String> masterSheetName = masterService.findSheetNameForMaster(master.get().masterCode());

        if (masterSheetName.isEmpty()) {
            sender.sendMessage(chatId,"Не смог найти этого мастера, попробуйте снова");
            return Optional.empty();
        }
        if (bookingSession.get().getSelectedSlot() != null) {
             dates = scheduleService.getFreeDatesForMasterBySlot(master.get().masterCode(),bookingSession.get().getSelectedSlot());
        }
        if (dates.isEmpty()) {
            sender.sendMessage(chatId,"У этого мастера нет свободных дат на месяц вперёд, выберите другого");
            return Optional.empty();
        }

        List<InlineKeyboardButton> buttons = dates.stream()
                .map(date-> button(date.format(DATE_FORMATTER),CallbackData.date(date.format(DATE_FORMATTER))))
                .toList();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(splitButtonsByRows(buttons,2));
        return Optional.of(markup);
    }

    public Optional<InlineKeyboardMarkup> showFreeBookingDatesForMaster(Long chatId,Long telegramUserId) {
        Optional<BookingSession> bookingSession = bookingSessionService.findByTelegramUserId(telegramUserId);
        if (bookingSession.isEmpty()) {
            sender.sendMessage(chatId,"Проблемы с бронированием, попробуйте снова");
            return Optional.empty();
        }
        Optional<Master> master = masterService.findByMasterCode(bookingSession.get().getSelectedMasterCode());
        if (master.isEmpty()) {
            sender.sendMessage(chatId,"Мастер не найден. Попробуйте начать бронирование снова");
            return Optional.empty();
        }
        Set<LocalDate> dates = scheduleService.getFreeDatesForMaster(master.get().masterCode());
        if (dates.isEmpty()) {
            sender.sendMessage(chatId,"У этого мастера нет свободных дат на месяц вперёд, выберите другого");
            return Optional.empty();
        }
        List<InlineKeyboardButton> buttons = dates.stream()
                .map(date-> button(date.format(DATE_FORMATTER),CallbackData.date(date.format(DATE_FORMATTER))))
                .toList();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(splitButtonsByRows(buttons,2));
        return Optional.of(markup);
    }

    public InlineKeyboardMarkup showBookingDateOrTime(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow((button(DATE_BUTTON_TEXT,CallbackData.dateOrSlotMode("date")))),
                new InlineKeyboardRow(button(TIME_BUTTON_TEXT,CallbackData.dateOrSlotMode("slot")))
        ));
        return markup;
    }

    public Optional<InlineKeyboardMarkup> showFreeBookingSlotForMasterByDate(Long chatId,String masterCode,LocalDate date) {
        Set<Slot> slots = scheduleService.getFreeBookingSlotsForMasterByDate(masterCode,date);
        if (slots.isEmpty()) {
            sender.sendMessage(chatId,"У мастера нет свободных слотов для игр в этот день. Пожалуйста выберите другую дату");
            return Optional.empty();
        }
        List<InlineKeyboardButton> buttons = List.of(Slot.FIRST,Slot.SECOND).stream()
                .filter(slots::contains)
                .map(slot -> button(formatSlot(slot),CallbackData.slot(slot)))
                .toList();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(splitButtonsByRows(buttons,buttons.size()));
        return Optional.of(markup);
    }

    public InlineKeyboardMarkup showConfirm(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(
                new InlineKeyboardRow(button(CONFIRM,CallbackData.CONFIRMATION))
        ));
        return markup;
    }

    private List<InlineKeyboardRow> splitButtonsByRows(List<InlineKeyboardButton> buttons, int buttonsInRow) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (int i = 0; i < buttons.size(); i += buttonsInRow) {
            if (i + 1 < buttons.size()) {
                rows.add(new InlineKeyboardRow(buttons.get(i), buttons.get(i + 1)));
            } else {
                rows.add(new InlineKeyboardRow(buttons.get(i)));
            }
        }
        return rows;
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    private String formatSlot(Slot slot) {
        if (slot == Slot.FIRST) {
            return "12:00-17:00";
        }
        return "18:00-23:00";
    }
}
