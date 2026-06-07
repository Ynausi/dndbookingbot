package ru.ynausi.dndbookingbot.session;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminMasterSessionServiceImpl {
    private final Map<Long,AdminMasterSession> sessionMap = new ConcurrentHashMap<>();

    public void startSession(Long telegramUserId,String role) {
        AdminMasterSession session = new AdminMasterSession();
        session.setTelegramUserId(telegramUserId);
        session.setRole(role);
        sessionMap.put(telegramUserId,session);
    }

    public Optional<AdminMasterSession> findSessionByTelegramUserId(Long telegramUserId) {
        return Optional.ofNullable(sessionMap.get(telegramUserId));
    }

}
