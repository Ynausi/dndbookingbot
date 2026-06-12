package ru.ynausi.dndbookingbot.master;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import ru.ynausi.dndbookingbot.googleSheets.GSMasterService;
import ru.ynausi.dndbookingbot.schedule.Slot;
//import ru.ynausi.dndbookingbot.googleSheets.GoogleSheetsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
@CacheConfig(cacheNames = "masters")
public class MasterRepositoryImpl implements MasterRepository{
    private final GSMasterService gsMasterService;

    @Override
    @Cacheable
    public List<Master> getMasters() {
        log.info("Читаю лист мастеров из Google Sheets");
        return gsMasterService.getMastersInfo();
    }

    @CacheEvict(allEntries = true)
    @Override
    public void updateMasterTelegramIdAndChatId(Long telegramUserId, Long chatId, Master master) {
        gsMasterService.updateMasterTelegramIdAndChatId(telegramUserId,chatId,master);
    }

    @CacheEvict(allEntries = true)
    @Override
    public void updateMasterPhoto(String fileId, Master master) {
        gsMasterService.updateMasterPhoto(fileId,master);
    }

}
