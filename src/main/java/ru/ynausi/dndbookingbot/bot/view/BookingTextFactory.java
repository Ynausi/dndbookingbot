package ru.ynausi.dndbookingbot.bot.view;

import lombok.Builder;
import org.springframework.stereotype.Component;
import ru.ynausi.dndbookingbot.booking.BookingSession;
import ru.ynausi.dndbookingbot.booking.Steps;
import ru.ynausi.dndbookingbot.bot.TelegramSender;
import ru.ynausi.dndbookingbot.master.Master;
import ru.ynausi.dndbookingbot.master.MasterService;
import ru.ynausi.dndbookingbot.schedule.Slot;

import java.util.Optional;

@Component
@Builder
public class BookingTextFactory {
    private final TelegramSender sender;
    private final MasterService masterService;

    private static final String START_MESSAGE ="""
                🎲 Бронирование игры
                
                Мастер:
                Дата: не выбрано
                Время: не выбрано
                """;

    public BookingTextFactory(TelegramSender sender, MasterService masterService) {
        this.sender = sender;
        this.masterService = masterService;
    }

    public String buildMasterText(BookingSession bookingSession) {
        Optional<Master> master = masterService.findByMasterCode(bookingSession.getSelectedMasterCode());
        String masterName = master.map(Master::name).orElse("Не найден");
        String date = bookingSession.getSelectedDate() == null
                ? "Не выбрана"
                : bookingSession.getSelectedDate().toString();
        String time = bookingSession.getSelectedSlot() == null
                ? "Не выбрано"
                : formatSlot(bookingSession.getSelectedSlot());
        String message = "";
        if (bookingSession.getStep() == Steps.DATE) message ="Выберите дату";
        if (bookingSession.getStep() == Steps.SLOT) message = "Выберите время";
        if (bookingSession.getStep() == Steps.BOOKING_MODE) message = "С чего начнём бронирование?";
        if (bookingSession.getStep() == Steps.CONFIRM) message = "Подтвердите запись";
        return """
                🎲 Бронирование игры
                
                Мастер: %s
                Дата: %s
                Время: %s
                
                %s
                """.formatted(masterName,date,time,message);
    }

    private String formatSlot(Slot slot) {
        if (slot == Slot.FIRST) {
            return "12:00-17:00";
        }
        return "18:00-23:00";
    }
}
