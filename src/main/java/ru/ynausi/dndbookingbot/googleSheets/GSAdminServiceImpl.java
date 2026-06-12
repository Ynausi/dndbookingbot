package ru.ynausi.dndbookingbot.googleSheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ynausi.dndbookingbot.admin.Admin;
import ru.ynausi.dndbookingbot.configuration.GoogleSheetsProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor

public class GSAdminServiceImpl implements GSAdminService{
    private final Sheets sheets;
    private final GoogleSheetsProperties properties;

    @Override
    public void updateAdminTelegramIdAndChatId(Long telegramUserId, Long chatId) {
        log.info("Пробую записать админа: telegramUserId={}, chatId={}",
                telegramUserId, chatId);
        try {
            List<List<Object>> rows = getAdminRows();
            List<String> updatedRows = new ArrayList<>();
            Integer iter =1;
            for (List<Object> row:rows) {
                String currentRow = getCell(row,0);
                if (currentRow.equals("telegramUserId")) updatedRows.add(iter.toString());
                if (currentRow.equals("chatId")) updatedRows.add(iter.toString());
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
        } catch (IOException e ) {
            log.error("Ошибка при записи данных админа в Google Sheets", e);
        }
    }

    @Override
    public void updatePhotoOnAdminList(Long telegramId, String fileId,String photoName) {
        log.info("Пробую обновить фото");
        List<List<Object>> rows = getAdminRows();
        List<String> updatedRows = new ArrayList<>();
        Integer iter =1;
        for (List<Object> row:rows) {
            String currentRow = getCell(row, 0);
            if (currentRow.equals(photoName)) updatedRows.add(iter.toString());
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
        } catch (IOException e ) {
            log.error("Ошибка при записи данных админа в Google Sheets", e);
        }
    }

    @Override
    public boolean checkIfAdmin(String adminCode) {
        List<List<Object>> rows = getAdminRows();
        for (List<Object> row:rows) {
            String currentRow = getCell(row,1);
            if (currentRow.equals(adminCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<List<Object>> getAdminRows() {
        log.info("Читаю лист админа");
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

    @Override
    public Optional<Admin> getAdmin() {
        log.info("Читаю лист админа и вывожу админа");
        try {
            String range = "Admin!A:B";
            ValueRange response = sheets.spreadsheets()
                    .values()
                    .get(properties.spreadSheetId(), range)
                    .execute();
            List<List<Object>> rows =  response.getValues();
            Admin admin = new Admin(
                    getCell(rows, 0, 1),
                    getCell(rows, 1, 1),
                    getCell(rows, 2, 1),
                    getCell(rows, 3, 1),
                    getCell(rows, 4, 1),
                    getCell(rows, 5, 1)
            );
            return Optional.of(admin);
        } catch (IOException e) {
            log.error("Ошибка при чтении данных из листа админа",e);
            return Optional.empty();
        }
    }

    private String getCell(List<List<Object>> rows, int rowIndex, int columnIndex) {
        if (rows == null || rows.size() <= rowIndex) {
            return "";
        }

        List<Object> row = rows.get(rowIndex);

        if (row == null || row.size() <= columnIndex || row.get(columnIndex) == null) {
            return "";
        }

        return row.get(columnIndex).toString();
    }

    private String getCell(List<Object> row,int index) {
        if(row.size()<=index){
            return "";
        }
        Object value = row.get(index);
        return value ==null?"":value.toString();
    }
}
