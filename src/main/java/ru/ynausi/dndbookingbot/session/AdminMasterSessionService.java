package ru.ynausi.dndbookingbot.session;

import java.util.Optional;

public interface AdminMasterSessionService {

    void startSession(Long telegramUserId,String role);

    Optional<AdminMasterSession> findSessionByTelegramUserId(Long telegramUserId);



}
