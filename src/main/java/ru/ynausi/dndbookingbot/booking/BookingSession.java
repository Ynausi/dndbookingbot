package ru.ynausi.dndbookingbot.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingSession {
    private Long userId;
    private Long chatId;
    private String userName;
    private String selectedMasterCode;
    private String selectedDate;
    private String selectedSlot;
    private String step;
}
