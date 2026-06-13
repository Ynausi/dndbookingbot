package ru.ynausi.dndbookingbot.master;

import java.util.List;

public interface GSMasterService {

    List<Master> getMastersInfo();

    boolean updateMasterTelegramIdAndChatId(Long telegramUserId,Long chatId,Master master);

    boolean updateMasterPhoto(String  fileId,Master master);

}
