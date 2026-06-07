package ru.ynausi.dndbookingbot.googleSheets;

import org.springframework.stereotype.Component;
import ru.ynausi.dndbookingbot.admin.Admin;
import ru.ynausi.dndbookingbot.master.Master;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoogleSheetsService {

    List<Master> getMastersInfo();

    void updateMasterTelegramIdAndChatId(Long telegramUserId,Long chatId,Integer rowNumber);

    void updateMasterPhoto(String  fileId,Integer rowNumber);

    Optional<Integer> findMasterRowByTelegramId(Long telegramUserId, Long chatId);

    List<String> findDatesForMaster(LocalDate start,LocalDate end,String masterCode,String selectedSlot);

    void addGameToMastersList(String selectedSlot,String sheetName,String date,String userName);

    void updateAdminTelegramIdAndChatId(Long telegramUserId,Long chatId);

    void updatePhotoOnAdminList(Long telegramId,String fileId,String photoName);

    boolean checkIfAdmin(String adminCode);

    List<List<Object>> getAdminRows();

    Optional<Admin> getAdmin();
}
