package ru.ynausi.dndbookingbot.master;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import ru.ynausi.dndbookingbot.googleSheets.GoogleSheetsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
@CacheConfig(cacheNames = "masters")
public class MasterRepositoryImpl implements MasterRepository{
    private final GoogleSheetsService googleSheetsService;

    @Override
    @Cacheable
    public List<Master> getMasters() {
        log.info("Читаю мастеров из Google Sheets");
        return googleSheetsService.getMastersInfo();
    }

    @CacheEvict(allEntries = true)
    @Override
    public void updateMasterTelegramIdAndChatId(Long telegramUserId, Long chatId, Integer rowNumber) {
        googleSheetsService.updateMasterTelegramIdAndChatId(telegramUserId,chatId,rowNumber);
    }

    @CacheEvict(allEntries = true)
    @Override
    public void updateMasterPhoto(String fileId, Integer rowNumber) {
        googleSheetsService.updateMasterPhoto(fileId,rowNumber);
    }

    @Override
    public Optional<Integer> findMasterRowByTelegramId(Long telegramUserId, Long chatId) {
        return googleSheetsService
                .findMasterRowByTelegramId(telegramUserId,chatId);
    }

    @Override
    public List<LocalDate> getMasterRowByTelegramId(LocalDate start, LocalDate end, String masterCode) {
        return List.of();
    }

    @Override
    public List<String> getDatesForMaster(LocalDate start, LocalDate end, String masterCode,String selectedSlot) {
        return googleSheetsService.findDatesForMaster(start,end,masterCode,selectedSlot);
    }

    @Override
    public void addGameToMasterList(String selectedSlot, String sheetName, String date, String userName) {
        googleSheetsService.addGameToMastersList(selectedSlot,sheetName,date,userName);
    }
}
