package ru.ynausi.dndbookingbot.schedule;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Repository
@Slf4j
@CacheConfig(cacheNames = "schedule")
@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements SceduleRepository{
    private final GSScheduleService gsScheduleService;


    @Override
    @Cacheable
    public Map<String, Map<LocalDate, DaySchedule>> getMastersSchedule() {
        log.info("Читаю листы мастеров");
        return gsScheduleService.readMastersSchedule();
    }

    @Override
    @CacheEvict(allEntries = true)
    public boolean updateMasterSchedule(String masterCode, LocalDate date, Slot slot,String userName) {
        return gsScheduleService.updateMasterSchedule(slot,masterCode,date,userName);
    }

}
