package ru.ynausi.dndbookingbot.booking;

import java.util.Optional;

public interface BookingSessionService {

    void startSession(Long chatId,Long telegramUserId,String userName,String masterCode);

    boolean selectSlot(Long telegramUserId,String selectedSlot);

    boolean selectDate(Long telegramUserId,String date);

    Optional<BookingSession> findByTelegramUserId(Long telegramUserId);
}
