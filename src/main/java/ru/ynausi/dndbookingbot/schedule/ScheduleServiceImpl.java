package ru.ynausi.dndbookingbot.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;


@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService{
    private final SceduleRepository sceduleRepository;


    @Override
    public Map<String, Map<LocalDate, DaySchedule>> getMastersSchedule() {
        return sceduleRepository.getMastersSchedule();
    }

    @Override
    public Map<LocalDate, DaySchedule> getMasterSchedule(String masterCode) {
        return sceduleRepository.getMastersSchedule().get(masterCode);
    }

    @Override
    public Set<LocalDate> getFreeDatesForMasterBySlot(String masterCode,Slot slot) {
        Set<LocalDate> result = new TreeSet<>();
        Map<String, Map<LocalDate, DaySchedule>> schedule = sceduleRepository.getMastersSchedule();
        Map<LocalDate,DaySchedule> masterSchedule= schedule.get(masterCode);
        Set<LocalDate> dates = masterSchedule.keySet();
        for (LocalDate date:dates) {
            if (slot == Slot.FIRST) {
                if ("+".equals(masterSchedule.get(date).firstSlotValue())) {
                    result.add(date);
                }
            }
            if (slot == Slot.SECOND) {
                if ("+".equals(masterSchedule.get(date).secondSlotValue())) {
                    result.add(date);
                }
            }
        }
        return result;
    }

    @Override
    public Set<LocalDate> getFreeDatesForMaster(String masterCode) {
        Set<LocalDate> result = new TreeSet<>();
        Map<String, Map<LocalDate, DaySchedule>> schedule = sceduleRepository.getMastersSchedule();
        Map<LocalDate,DaySchedule> masterSchedule= schedule.get(masterCode);
        Set<LocalDate> dates = masterSchedule.keySet();
        for (LocalDate date:dates) {
            DaySchedule daySchedule = masterSchedule.get(date);
            if ("+".equals(daySchedule.firstSlotValue())) result.add(date);
            if ("+".equals(daySchedule.secondSlotValue())) result.add(date);
        }
        return result;
    }

    @Override
    public Set<Slot> getFreeSlotsForMasterByDate(String masterCode, LocalDate date) {
        Set<Slot> result = new HashSet<>();
        Map<String, Map<LocalDate, DaySchedule>> schedule = sceduleRepository.getMastersSchedule();
        Map<LocalDate,DaySchedule> masterSchedule= schedule.get(masterCode);
        DaySchedule daySchedule = masterSchedule.get(date);
        if ("+".equals(daySchedule.firstSlotValue())) result.add(Slot.FIRST);
        if ("+".equals(daySchedule.secondSlotValue())) result.add(Slot.SECOND);
        return result;
    }

    @Override
    public Set<Slot> getFreeBookingSlotsForMasterByDate(String masterCode, LocalDate date) {
        Set<Slot> result = new HashSet<>();
        Map<String, Map<LocalDate, DaySchedule>> schedule = sceduleRepository.getMastersSchedule();
        Map<LocalDate,DaySchedule> masterSchedule= schedule.get(masterCode);
        DaySchedule daySchedule = masterSchedule.get(date);
        if ("+".equals(daySchedule.firstSlotValue())) result.add(Slot.FIRST);
        if ("+".equals(daySchedule.secondSlotValue())) result.add(Slot.SECOND);
        return result;
    }

    @Override
    public boolean updateMasterSchedule(String masterCode, LocalDate date, Slot slot, String userName) {
        return sceduleRepository.updateMasterSchedule(masterCode,date,slot,userName);
    }


}
