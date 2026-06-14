package ru.ynausi.dndbookingbot.master;

import java.util.List;
import java.util.Optional;

public interface MasterService {

    List<Master> getMasters();

    Optional<Master> getById(String id);

    Optional<Master> findByMasterCode(String masterCode);

    List<Master> getActiveMasters();

    void updateMasterTelegramIdAndChatId(Long telegramUserId,Long chatId,Master master);

    void updateMasterPhoto(String fileId,Master master);

    Optional<String> findSheetNameForMaster(String masterCode);

    Optional<Master> findMasterByTelegramId(Long telegramUserId);
}
