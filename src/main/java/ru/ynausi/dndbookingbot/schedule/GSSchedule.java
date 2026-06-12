package ru.ynausi.dndbookingbot.schedule;

import java.time.LocalDate;
import java.util.Map;

public interface GSSchedule {

    Map<String, Map<LocalDate,DaySchedule>> readMastersSchedule();

    boolean updateMasterSchedule(Slot selectedSlot, String sheetName, LocalDate date, String userName);
}
