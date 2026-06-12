package ru.ynausi.dndbookingbot.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ynausi.dndbookingbot.master.Master;
import ru.ynausi.dndbookingbot.master.MasterService;

import java.time.LocalDate;
import java.util.*;


@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService{
    private final SceduleRepository sceduleRepository;
    private final MasterService masterService;

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

    @Override
    public Set<LocalDate> getFreeDatesForAllMasters() {
        Map<String,Map<LocalDate,DaySchedule>> activeMastersSchedule = getActiveMastersSchedule();
        List<Master> masters = masterService.getActiveMasters();
        Set<LocalDate> result = new TreeSet<>();
        for (Master master:masters) {
            String masterCode = master.masterCode();
            Map<LocalDate,DaySchedule> masterSchedule = activeMastersSchedule.get(masterCode);
            Set<LocalDate> mastersDate = masterSchedule.keySet();
            for (LocalDate date:mastersDate) {
                DaySchedule daySchedule = masterSchedule.get(date);
                if ("+".equals(daySchedule.firstSlotValue())) result.add(date);
                if("+".equals(daySchedule.secondSlotValue())) result.add(date);
            }
        }
        return result;
    }

    @Override
    public Set<Slot> findFreeBookingSlotsForDate(LocalDate date) {
        Map<String,Map<LocalDate,DaySchedule>> activeMastersSchedule = getActiveMastersSchedule();
        List<Master> masters = masterService.getActiveMasters();
        Set<Slot> result = new TreeSet<>();
        for (Master master:masters) {
            String masterCode = master.masterCode();
            Map<LocalDate,DaySchedule> masterSchedule = activeMastersSchedule.get(masterCode);
            DaySchedule daySchedule = masterSchedule.get(date);
            if ("+".equals(daySchedule.firstSlotValue())) result.add(Slot.FIRST);
            if("+".equals(daySchedule.secondSlotValue())) result.add(Slot.SECOND);
        }
        return result;
    }

    @Override
    public List<Master> findFreeMastersForDateAndSlot(LocalDate date, Slot slot) {
        Map<String,Map<LocalDate,DaySchedule>> activeMastersSchedule = getActiveMastersSchedule();
        List<Master> masters = masterService.getActiveMasters();
        List<Master> result = new ArrayList<>();
        for (Master master:masters) {
            String masterCode = master.masterCode();
            Map<LocalDate,DaySchedule> masterSchedule = activeMastersSchedule.get(masterCode);
            if (masterSchedule == null) {
                continue;
            }
            DaySchedule daySchedule = masterSchedule.get(date);
            if (daySchedule == null) {
                continue;
            }
            if (daySchedule.isFree(slot)) {
                result.add(master);
            }
        }
        return result;
    }

    @Override
    public Map<String, Map<LocalDate, DaySchedule>> getActiveMastersSchedule() {
        Map<String,Map<LocalDate,DaySchedule>> allMastersSchedule = sceduleRepository.getMastersSchedule();
        List<Master> masters = masterService.getActiveMasters();
        Map<String,Map<LocalDate,DaySchedule>> allActiveMastersSchedule = new LinkedHashMap<>();
        for (Master master:masters) {
            String masterCode = master.masterCode();
            if (master.active().equals(true)) allActiveMastersSchedule.put(masterCode,allMastersSchedule.get(masterCode));
        }
        return allActiveMastersSchedule;
    }



}
