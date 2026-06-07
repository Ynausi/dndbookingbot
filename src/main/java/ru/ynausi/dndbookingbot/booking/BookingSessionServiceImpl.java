package ru.ynausi.dndbookingbot.booking;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookingSessionServiceImpl implements BookingSessionService{
    private final Map<Long,BookingSession> booking = new ConcurrentHashMap<>();


    public void startSession(Long chatId,Long telegramUserId,String userName,String masterCode) {
        BookingSession bookingSession = new BookingSession();
        bookingSession.setUserId(telegramUserId);
        bookingSession.setChatId(chatId);
        bookingSession.setUserName(userName);
        bookingSession.setSelectedMasterCode(masterCode);
        bookingSession.setStep("SELECTING_TIME");
        booking.put(telegramUserId,bookingSession);
    }

    public boolean selectSlot(Long telegramUserId,String selectedSlot) {
        BookingSession bookingSession = booking.get(telegramUserId);
        if (bookingSession == null) {
            return false;
        }
        bookingSession.setSelectedSlot(selectedSlot);
        bookingSession.setStep("SELECTING_DATE");
        return true;
    }

    public boolean selectDate(Long telegramUserId,String date) {
        BookingSession bookingSession = booking.get(telegramUserId);
        if (bookingSession == null) {
            return false;
        }
        bookingSession.setSelectedDate(date);
        bookingSession.setStep("CONFIRMATION");
        return true;
    }

    public Optional<BookingSession> findByTelegramUserId(Long telegramUserId) {
        return Optional.ofNullable(booking.get(telegramUserId));
    }

}
