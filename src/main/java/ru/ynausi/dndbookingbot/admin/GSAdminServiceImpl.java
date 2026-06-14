package ru.ynausi.dndbookingbot.admin;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ynausi.dndbookingbot.configuration.GoogleSheetsProperties;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor

public class GSAdminServiceImpl implements GSAdminService {
    private final Sheets sheets;
    private final GoogleSheetsProperties properties;

    @Override
    public Optional<Admin> getAdmin() {
        log.info("Читаю лист админа");
        List<List<Object>> rows = getAdminRows();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Map<String, String> adminData = new HashMap<>();
        for (List<Object> row : rows) {
            String key = getCell(row, 0);
            String value = getCell(row, 1);

            if (!key.isBlank()) {
                adminData.put(key, value);
            }
        }
        Admin admin = Admin.builder()
                .telegramUserId(Long.valueOf(adminData.get("telegramUserId")))
                .chatId(Long.valueOf(adminData.get("chatId")))
                .adminCode(adminData.get("code"))
                .adventurePhotoId(adminData.get("adventure"))
                .oneShotPhotoId(adminData.get("oneshot"))
                .companyPhotoId(adminData.get("company"))
                .build();
        log.info("Прочитал админа и добавил в кэш");
        return Optional.of(admin);
    }

    @Override
    public boolean updateAdminTelegramIdAndChatId(Long telegramUserId, Long chatId) {
        log.info("Пробую записать админа: telegramUserId={}, chatId={}",
                telegramUserId, chatId);
        try {
            List<List<Object>> rows = getAdminRows();
            List<String> updatedRows = new ArrayList<>();
            int iter =1;
            for (List<Object> row:rows) {
                String currentRow = getCell(row,0);
                if (currentRow.equals("telegramUserId")) updatedRows.add(Integer.toString(iter));
                if (currentRow.equals("chatId")) updatedRows.add(Integer.toString(iter));
                iter++;
            }
            String range = "Admin!B"+updatedRows.getFirst()+":B"+updatedRows.getLast();
            ValueRange body = new ValueRange().setValues(List.of(
                    List.of(telegramUserId.toString()),
                    List.of(chatId.toString())
            ));
            UpdateValuesResponse result =  sheets.spreadsheets().values()
                    .update(properties.spreadSheetId(), range, body)
                    .setValueInputOption("RAW")
                    .execute();
            log.info("Обновлено ячеек: {}", result.getUpdatedCells());
            return true;
        } catch (IOException e ) {
            log.error("Ошибка при записи данных админа в Google Sheets", e);
        }
        return false;
    }

    @Override
    public boolean updatePhotoOnAdminList(String fileId,String photoName) {
        log.info("Пробую обновить фото");
        List<List<Object>> rows = getAdminRows();
        List<String> updatedRows = new ArrayList<>();
        int iter =1;
        for (List<Object> row:rows) {
            String currentRow = getCell(row, 0);
            if (currentRow.equals(photoName)) updatedRows.add(Integer.toString(iter));
            iter++;
        }
        String range = "Admin!B"+updatedRows.getFirst();
        ValueRange body = new ValueRange().setValues(List.of(
                List.of(fileId)
        ));
        try {
            UpdateValuesResponse result = sheets.spreadsheets().values()
                    .update(properties.spreadSheetId(), range,body)
                    .setValueInputOption("RAW")
                    .execute();
            log.info("Обновлено ячеек: {}", result.getUpdatedCells());
            return true;
        } catch (IOException e ) {
            log.error("Ошибка при записи данных админа в Google Sheets", e);
        }
        return false;
    }

    private List<List<Object>> getAdminRows() {
        try {
            String range = "Admin!A:B";
            ValueRange response = sheets.spreadsheets()
                    .values()
                    .get(properties.spreadSheetId(), range)
                    .execute();
            return response.getValues();
        } catch (IOException e) {
            log.error("Ошибка при чтении данных из листа админа",e);
            return List.of();
        }
    }

    private String getCell(List<Object> row,int index) {
        if(row.size()<=index){
            return "";
        }
        Object value = row.get(index);
        return value ==null?"":value.toString();
    }
}
