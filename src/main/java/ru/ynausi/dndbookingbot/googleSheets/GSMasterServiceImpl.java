package ru.ynausi.dndbookingbot.googleSheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ynausi.dndbookingbot.configuration.GoogleSheetsProperties;
import ru.ynausi.dndbookingbot.master.Master;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GSMasterServiceImpl implements GSMasterService{
    private final Sheets sheets;
    private final GoogleSheetsProperties properties;

    @Override
    public List<Master> getMastersInfo() {
        try {
            String range = "Masters!A1:Z";
            ValueRange response = sheets.spreadsheets()
                    .values()
                    .get(properties.spreadSheetId(), range)
                    .execute();
            List<List<Object>> rows = response.getValues();
            List<Object> headerRows = rows.getFirst();
            /// //Формирую список заголовков
            Map<String,Integer> headerMap = new HashMap<>();
            int i = 0;
            for (Object header : headerRows) {
                headerMap.put(header.toString(), i);
                i++;
            }
            if (headerMap.isEmpty()) {
                log.error("Ошибка при чтении заголовков в листе Мастеров");
                return List.of();
            }
            ///
            List<Master> masters = new ArrayList<>();
            for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
                List<Object> row = rows.get(rowIndex);
                int currentRowNumber =rowIndex+1;
                Master master = Master.builder()
                        .id(Integer.parseInt(getCell(row, headerMap.get("id"))))
                        .name(getCell(row, headerMap.get("name")))
                        .masterCode(getCell(row, headerMap.get("masterCode")))
                        .description(getCell(row, headerMap.get("description")))
                        .sheetName(getCell(row, headerMap.get("sheetName")))
                        .active(Boolean.parseBoolean(getCell(row, headerMap.get("active"))))
                        .telegramUserId(parseLongOrNull(getCell(row, headerMap.get("telegramUserId"))))
                        .chatId(parseLongOrNull(getCell(row, headerMap.get("chatId"))))
                        .photoFileId(getCell(row, headerMap.get("photoFileId")))
                        .smallDescription(getCell(row, headerMap.get("smallDescription")))
                        .rowNumber(currentRowNumber)
                        .build();
                masters.add(master);
                currentRowNumber++;
            }
            return masters;
        } catch (IOException e) {
            log.error("Ошибка при чтении Google Sheets",e);
            return List.of();
        }
    }

    @Override
    public void updateMasterTelegramIdAndChatId(Long telegramUserId,Long chatId,Master master) {
        log.info("Пробую записать мастера: telegramUserId={}, chatId={}, masterName={}",
                telegramUserId, chatId, master.name());
        Map<String,Integer> headerMap = readHeader();
        Integer indexTelegramUserId = headerMap.get("telegramUserId");
        Integer indexChatId = headerMap.get("chatId");
        String telegramUserIdRange = "Masters!" + columnIndexToLetter(indexTelegramUserId)  + master.rowNumber();
        String chatIdRange = "Masters!" + columnIndexToLetter(indexChatId) + master.rowNumber();
        ValueRange telegramUserIdBody = new ValueRange().setValues(List.of(
                List.of(telegramUserId.toString())
        ));
        ValueRange chatIdBody = new ValueRange().setValues(List.of(
                List.of(chatId.toString())
        ));
        try {
            UpdateValuesResponse resultTelegramId =  sheets.spreadsheets().values()
                    .update(properties.spreadSheetId(), telegramUserIdRange, telegramUserIdBody)
                    .setValueInputOption("RAW")
                    .execute();
            UpdateValuesResponse resultChatId = sheets.spreadsheets().values()
                            .update(properties.spreadSheetId(), chatIdRange,chatIdBody)
                            .setValueInputOption("RAW")
                            .execute();
            log.info("Обновлено ячеек telegramId: {}", resultTelegramId.getUpdatedCells());
            log.info("Обновлено ячеек chatId: {}",resultChatId.getUpdatedCells());
        } catch (IOException e ) {
            log.error("Ошибка при записи данных мастера в Google Sheets", e);
        }
    }

    @Override
    public void updateMasterPhoto(String fileId,Master master) {
        log.info("Пробую записать мастера: fileId={}",
                fileId);
        Map<String,Integer> headerMap = readHeader();
        Integer indexPhotoFileId = headerMap.get("photoFileId");
        String range = "Masters!"+columnIndexToLetter(indexPhotoFileId) + master.rowNumber();
        ValueRange body = new ValueRange().setValues(List.of(
                List.of(fileId)
        ));
        try {
            UpdateValuesResponse result = sheets.spreadsheets().values()
                    .update(properties.spreadSheetId(),range,body)
                    .setValueInputOption("RAW")
                    .execute();
            log.info("Обновлено ячеек: {}", result.getUpdatedCells());
        } catch (IOException e) {
            log.error("Ошибка при записи данных мастера в Google Sheets", e);
        }
    }

    private Long parseLongOrNull(String value) {
        if (value.isEmpty()) {
            return null;
        }
        return Long.parseLong(value.trim());
    }

    private Map<String,Integer> readHeader() {
        try {
            String range = "Masters!A1:Z";
            ValueRange response = sheets.spreadsheets()
                    .values()
                    .get(properties.spreadSheetId(), range)
                    .execute();
            List<List<Object>> rows = response.getValues();
            List<Object> headerRows = rows.getFirst();
            Map<String,Integer> reuslt = new HashMap<>();
            int i = 0;
            for (Object header : headerRows) {
                reuslt.put(header.toString(), i);
                i++;
            }
            return reuslt;
        } catch (IOException e) {
            log.error("Ошибка при чтении заголовков мастеров",e);
        }
        return Map.of();
    }

    private String columnIndexToLetter(int columnIndex) {
        StringBuilder columnName = new StringBuilder();

        int number = columnIndex + 1; // переводим из 0-based в 1-based

        while (number > 0) {
            int remainder = (number - 1) % 26;
            columnName.insert(0, (char) ('A' + remainder));
            number = (number - 1) / 26;
        }

        return columnName.toString();
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
