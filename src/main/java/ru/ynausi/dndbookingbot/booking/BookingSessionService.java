package ru.ynausi.dndbookingbot.booking;

import ru.ynausi.dndbookingbot.schedule.Slot;

import java.time.LocalDate;
import java.util.Optional;

public interface BookingSessionService {

    void startSession(Long chatId,Long telegramUserId,String userName,String masterCode);

    boolean selectSlot(Long telegramUserId, Slot selectedSlot);

    boolean selectDate(Long telegramUserId, LocalDate date);

    Optional<BookingSession> findByTelegramUserId(Long telegramUserId);
}
