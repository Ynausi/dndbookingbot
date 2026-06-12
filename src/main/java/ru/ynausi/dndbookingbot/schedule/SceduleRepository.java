package ru.ynausi.dndbookingbot.schedule;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public interface SceduleRepository {

    Map<String, Map<LocalDate, DaySchedule>> getMastersSchedule();

    boolean updateMasterSchedule(String masterCode,LocalDate date, Slot slot,String userName);


}
