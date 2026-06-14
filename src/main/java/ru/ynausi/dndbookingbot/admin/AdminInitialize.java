package ru.ynausi.dndbookingbot.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitialize {
    private final AdminService adminService;

    @Order(4)
    @EventListener(ApplicationReadyEvent.class)
    public void loadAdmin() {
        log.info("Читаю админа");
        adminService.getAdmin();
        log.info("Админ прочитан");
    }
}
