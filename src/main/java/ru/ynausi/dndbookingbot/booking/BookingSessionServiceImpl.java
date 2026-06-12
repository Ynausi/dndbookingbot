package ru.ynausi.dndbookingbot.booking;

import org.springframework.stereotype.Service;
import ru.ynausi.dndbookingbot.schedule.Slot;

import java.time.LocalDate;
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
        bookingSession.setStep(Steps.SLOT);
        booking.put(telegramUserId,bookingSession);
    }

    public void startSession(Long chatId,Long telegramUserId,String userName) {
        BookingSession bookingSession = new BookingSession();
        bookingSession.setUserId(telegramUserId);
        bookingSession.setChatId(chatId);
        bookingSession.setUserName(userName);
        booking.put(telegramUserId,bookingSession);
    }

    public boolean selectSlot(Long telegramUserId, Slot selectedSlot) {
        BookingSession bookingSession = booking.get(telegramUserId);
        if (bookingSession == null) {
            return false;
        }
        bookingSession.setSelectedSlot(selectedSlot);
        bookingSession.setStep(Steps.DATE);
        return true;
    }

    public boolean selectDate(Long telegramUserId, LocalDate date) {
        BookingSession bookingSession = booking.get(telegramUserId);
        if (bookingSession == null) {
            return false;
        }
        bookingSession.setSelectedDate(date);
        bookingSession.setStep(Steps.CONFIRM);
        return true;
    }

    public Optional<BookingSession> findByTelegramUserId(Long telegramUserId) {
        return Optional.ofNullable(booking.get(telegramUserId));
    }

    public boolean setStep(Long telegramUserId,Steps step) {
        BookingSession bookingSession = booking.get(telegramUserId);
        if (bookingSession == null) {
            return false;
        }
        bookingSession.setStep(step);
        return true;
    }

    public boolean delete(Long telegramUserId) {
        return booking.remove(telegramUserId) != null;
    }

}
