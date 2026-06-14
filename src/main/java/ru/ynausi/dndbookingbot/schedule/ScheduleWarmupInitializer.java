package ru.ynausi.dndbookingbot.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleWarmupInitializer {
    private final ScheduleService scheduleService;

    @Order(3)
    @EventListener(ApplicationReadyEvent.class)
    public void loadMastersSchedule(){
        log.info("Загружаю расписания активных мастеров в кэш");
        scheduleService.getActiveMastersSchedule();
        log.info("Расписания активных мастеров загружены");
    }
}
