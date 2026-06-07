package ru.ynausi.dndbookingbot.master;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MasterRepository {

    List<Master> getMasters();

    void updateMasterTelegramIdAndChatId(Long telegramUserId,Long chatId,Integer rowNumber);

    void updateMasterPhoto(String fileId,Integer rowNumber);

    Optional<Integer> findMasterRowByTelegramId(Long telegramId, Long chatId);

    List<LocalDate> getMasterRowByTelegramId(LocalDate start,LocalDate end,String masterCode);

    List<String> getDatesForMaster(LocalDate start,LocalDate end,String masterCode,String selectedSlot);

    void addGameToMasterList(String selectedSlot,String sheetName,String date,String userName);

}
