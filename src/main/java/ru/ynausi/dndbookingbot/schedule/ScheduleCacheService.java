package ru.ynausi.dndbookingbot.schedule;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ScheduleCacheService implements SceduleCache {
    private final GSScheduleService gsScheduleService;
    private final CacheManager cacheManager;
    private static final String SCHEDULE_CACHE_NAME = "schedule";
    private static final String ALL_SCHEDULE_KEY = "allMastersSchedule";

    @Override
    @Cacheable(cacheNames = SCHEDULE_CACHE_NAME, key = "'" + ALL_SCHEDULE_KEY +"'")
    public Map<String, Map<LocalDate, DaySchedule>> getMastersSchedule() {
        log.info("Читаю листы мастеров");
        return gsScheduleService.readMastersSchedule();
    }

    @Override
    public boolean updateMasterSchedule(String masterCode, LocalDate date, Slot slot,String userName) {
        boolean updated = gsScheduleService.updateMasterCell(masterCode,date,slot,userName);
        if (updated) {
            updateCachedSlot(masterCode,date,slot,userName);
        }
        return updated;
    }

    private void updateCachedSlot(String masterCode, LocalDate date, Slot slot, String userName) {
        Cache cache = cacheManager.getCache(SCHEDULE_CACHE_NAME);
        if (cache == null) {
            return;
        }
        Map<String,Map<LocalDate,DaySchedule>> schedule = cache.get(ALL_SCHEDULE_KEY,Map.class);
        if (schedule == null) {
            return;
        }
        Map<LocalDate,DaySchedule> masterSchedule = schedule.get(masterCode);
        if (masterSchedule == null) {
            return;
        }
        DaySchedule oldDaySchedule = masterSchedule.get(date);
        if(oldDaySchedule == null) {
            return;
        }
        DaySchedule newDaySchedule;
        if (slot == Slot.FIRST) {
            newDaySchedule = new DaySchedule(userName, oldDaySchedule.secondSlotValue());
        } else {
            newDaySchedule = new DaySchedule(oldDaySchedule.firstSlotValue(), userName);
        }
        Map<String, Map<LocalDate, DaySchedule>> newSchedule = new HashMap<>(schedule);
        Map<LocalDate, DaySchedule> newMasterSchedule = new TreeMap<>(masterSchedule);

        newMasterSchedule.put(date, newDaySchedule);
        newSchedule.put(masterCode, newMasterSchedule);
        cache.put(ALL_SCHEDULE_KEY, newSchedule);
    }

}
