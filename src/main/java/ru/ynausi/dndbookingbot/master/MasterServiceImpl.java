package ru.ynausi.dndbookingbot.master;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ynausi.dndbookingbot.googleSheets.GoogleSheetsService;

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
        return getMasters().stream()
                .filter(master -> master.id().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Master> findByMasterCode(String masterCode) {
        return getMasters().stream()
                .filter(master -> master.masterCode().equals(masterCode))
                .findFirst();
    }

    @Override
    public List<Master> getActiveMasters() {
        return masterRepository.getMasters().stream()
                .filter(master-> master.active().equals("TRUE"))
                .collect(Collectors.toList());
    }

    @Override
    public void updateMasterTelegramIdAndChatId(Long telegramUserId, Long chatId, Integer rowNumber) {
        masterRepository.updateMasterTelegramIdAndChatId(telegramUserId,chatId,rowNumber);
    }

    @Override
    public void updateMasterPhoto(String fileId, Integer rowNumber) {
       masterRepository.updateMasterPhoto(fileId,rowNumber);
    }

    @Override
    public List<String> findFreeDatesForMaster(LocalDate start, LocalDate end, String masterCode,String selectedSlot) {
        return masterRepository.getDatesForMaster(start,end,masterCode,selectedSlot);
    }

    @Override
    public Optional<Integer> findMasterRowByTelegramId(Long telegramId, Long chatId) {
        return masterRepository
                .findMasterRowByTelegramId(telegramId,chatId);
    }

    @Override
    public void addGameToMasterList(String selectedSlot, String sheetName, String date, String userName) {
        masterRepository.addGameToMasterList(selectedSlot,sheetName,date,userName);
    }


}
