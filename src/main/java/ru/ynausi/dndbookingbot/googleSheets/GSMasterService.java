package ru.ynausi.dndbookingbot.googleSheets;

import ru.ynausi.dndbookingbot.master.Master;
import ru.ynausi.dndbookingbot.schedule.Slot;

import java.time.LocalDate;
import java.util.List;

public interface GSMasterService {

    List<Master> getMastersInfo();

    void updateMasterTelegramIdAndChatId(Long telegramUserId,Long chatId,Master master);

    void updateMasterPhoto(String  fileId,Master master);

}
