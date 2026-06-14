package ru.ynausi.dndbookingbot.master;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MasterWarmupInitializer {
    private final MasterService masterService;

    @Order(2)
    @EventListener(ApplicationReadyEvent.class)
    public void loadActiveMasters(){
        log.info("Загружаю активных мастеров в кэш");
        masterService.getActiveMasters();
        log.info("Активные мастера загружены");
    }
}
