package ru.ynausi.dndbookingbot.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ynausi.dndbookingbot.schedule.Slot;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingSession {
    private Long userId;
    private Long chatId;
    private String userName;
    private String selectedMasterCode;
    private LocalDate selectedDate;
    private SessionMode sessionMode;
    private BookingModeDateOrSlot bookingModeDateOrSlot;
    private FirstStep firstStep;
    private Slot selectedSlot;
    private Steps step;
    private Integer masterListMessageId;
    private Integer masterViewMessageId;
    private Integer startMenuId;
}
