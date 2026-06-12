package ru.ynausi.dndbookingbot.master;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ynausi.dndbookingbot.schedule.Slot;
//import ru.ynausi.dndbookingbot.googleSheets.GoogleSheetsService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MasterServiceImpl implements MasterService{
    private final MasterRepository masterRepository;

    @Override
    public List<Master> getMasters() {
        return masterRepository.getMasters();
    }

    @Override
    public Optional<Master> getById(String id) {
        return masterRepository.getMasters().stream()
                .filter(master -> Integer.parseInt(id) == master.id())
                .findFirst();
    }

    @Override
    public Optional<Master> findByMasterCode(String masterCode) {
        return masterRepository.getMasters().stream()
                .filter(master -> masterCode.equals(master.masterCode()))
                .findFirst();
    }

    @Override
    public List<Master> getActiveMasters() {
        return masterRepository.getMasters().stream()
                .filter(master-> Boolean.TRUE.equals(master.active()))
                .collect(Collectors.toList());
    }

    @Override
    public void updateMasterTelegramIdAndChatId(Long telegramUserId, Long chatId, Master master) {
        masterRepository.updateMasterTelegramIdAndChatId(telegramUserId,chatId,master);
    }

    @Override
    public void updateMasterPhoto(String fileId, Master master) {
       masterRepository.updateMasterPhoto(fileId,master);
    }

    @Override
    public Optional<String> findSheetNameForMaster(String masterCode) {
        return masterRepository.getMasters().stream()
                .filter(master -> masterCode.equals(master.masterCode()))
                .map(Master::sheetName)
                .findFirst();
    }

    @Override
    public Optional<Master> findMasterByTelegramId(Long telegramUserId) {
        return masterRepository.getMasters().stream()
                .filter(master-> master.telegramUserId().equals(telegramUserId))
                .findFirst();
    }
}
