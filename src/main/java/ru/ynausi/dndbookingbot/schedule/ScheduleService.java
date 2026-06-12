package ru.ynausi.dndbookingbot.schedule;

import ru.ynausi.dndbookingbot.master.Master;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ScheduleService {

    Map<String, Map<LocalDate, DaySchedule>> getMastersSchedule();

     Map<LocalDate, DaySchedule> getMasterSchedule(String masterCode);

     Set<LocalDate> getFreeDatesForMasterBySlot(String masterCode, Slot slot);

     Set<LocalDate> getFreeDatesForMaster(String masterCode);

     Set<Slot> getFreeSlotsForMasterByDate(String masterCode,LocalDate date);

     Set<Slot> getFreeBookingSlotsForMasterByDate(String masterCode,LocalDate date);

     boolean updateMasterSchedule(String masterCode,LocalDate date, Slot slot,String userName);

     Set<LocalDate> getFreeDatesForAllMasters();

     Set<Slot> findFreeBookingSlotsForDate(LocalDate date);

     List<Master> findFreeMastersForDateAndSlot(LocalDate date, Slot slot);

     Map<String,Map<LocalDate,DaySchedule>> getActiveMastersSchedule();
}
