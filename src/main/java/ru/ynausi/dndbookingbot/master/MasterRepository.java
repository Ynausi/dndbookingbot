package ru.ynausi.dndbookingbot.master;

import ru.ynausi.dndbookingbot.schedule.Slot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MasterRepository {

    List<Master> getMasters();

    void updateMasterTelegramIdAndChatId(Long telegramUserId,Long chatId,Master master);

    void updateMasterPhoto(String fileId,Master master);

}
