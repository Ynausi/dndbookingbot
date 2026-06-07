package ru.ynausi.dndbookingbot.googleSheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.ynausi.dndbookingbot.admin.Admin;
import ru.ynausi.dndbookingbot.configuration.GoogleSheetsProperties;
import ru.ynausi.dndbookingbot.master.Master;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleSheetsServiceImpl implements GoogleSheetsService{
    private final Sheets sheets;
    private final GoogleSheetsProperties properties;

    @Override
    public List<Master> getMastersInfo() {
        try {
            String range = "Masters!A2:I";
            ValueRange response = sheets.spreadsheets()
                    .values()
                    .get(properties.spreadSheetId(), range)
                    .execute();
            List<List<Object>> rows = response.getValues();
            List<Master> masters = rows
                    .stream()
                    .map(row -> {
                           Master master = new Master(
                                   getCell(row,0),
                                   getCell(row,1),
                                   getCell(row,2),
                                   getCell(row,3),
                                   getCell(row,4),
                                   getCell(row,5),
                                   getCell(row,6),
                                   getCell(row,7),
                                   getCell(row,8)
                           );
                           return master;
                    }
                    ).collect(Collectors.toList());
            return masters;
        } catch (IOException e) {
            log.error("Ошибка при чтении Google Sheets",e);
            return List.of();
        }
    }

    @Override
    public void updateMasterTelegramIdAndChatId(Long telegramUserId,Long chatId,Integer rowNumber) {
        log.info("Пробую записать мастера: telegramUserId={}, chatId={}, rowNumber={}",
                telegramUserId, chatId, rowNumber);
        String range = "Masters!G"+rowNumber+":H" + rowNumber;
        ValueRange body = new ValueRange().setValues(List.of(
                List.of(telegramUserId.toString(), chatId.toString())
        ));
        try {
            UpdateValuesResponse result =  sheets.spreadsheets().values()
                    .update(properties.spreadSheetId(), range, body)
                    .setValueInputOption("RAW")
                    .execute();
            log.info("Обновлено ячеек: {}", result.getUpdatedCells());
        } catch (IOException e ) {
            log.error("Ошибка при записи данных мастера в Google Sheets", e);
        }
    }

    @Override
    public void updateMasterPhoto(String fileId,Integer rowNumber) {
        log.info("Пробую записать мастера: fileId={}",
                fileId);
        String range = "Masters!I"+rowNumber;
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

    @Override
    public Optional<Integer> findMasterRowByTelegramId(Long telegramUserId, Long chatId) {
        log.info("Пробую найти мастера по : telegramUserId={}",
                telegramUserId);
        try {
            String range = "Masters!G2:G";
            ValueRange response = sheets.spreadsheets()
                    .values()
                    .get(properties.spreadSheetId(), range)
                    .execute();
            List<List<Object>> rows = response.getValues();
            if(rows == null || rows.isEmpty()) {
                return Optional.empty();
            }
            Integer rowNumber =0;

            for (List<Object> row:rows) {
                String currentTelegramUserId = getCell(row,0);
                if(currentTelegramUserId.isBlank()) {
                    rowNumber++;
                    continue;
                }
                if (telegramUserId.toString().equals(currentTelegramUserId)) {
                    return Optional.of(rowNumber+2);
                }
                rowNumber++;
            }
            return Optional.empty();
        } catch (IOException e) {
            log.error("Ошибка при поиске данных",e);
            return Optional.empty();
        }
    }

    @Override
    public List<String> findDatesForMaster(LocalDate start, LocalDate end, String masterCode,String selectedSlot) {
        try {
            String range = "Masters!C2:C";
            ValueRange request = sheets.spreadsheets()
                    .values()
                    .get(properties.spreadSheetId(), range)
                    .execute();
            List<List<Object>> rows = request.getValues();
            int rowNumber = 2;
            for (List<Object> row: rows) {
                String currentMasterCode = getCell(row,0);
                if(currentMasterCode.isBlank()) {
                    rowNumber++;
                    continue;
                }
                if(currentMasterCode.equals(masterCode)) {
                     break;
                }
                rowNumber++;
            }
            String rangeSheetName = "Masters!E"+rowNumber;
            ValueRange requestSheetName = sheets.spreadsheets()
                    .values()
                    .get(properties.spreadSheetId(), rangeSheetName)
                    .execute();
            List<List<Object>> rows2 = requestSheetName.getValues();
            String sheetName = getCell(rows2.getFirst(),0);
            String rangeMasterSheet = sheetName +"!A2:C";
            int slotColumnIndex = 0;
            if (selectedSlot.equals("first")) {
                slotColumnIndex = 1;
            } else if (selectedSlot.equals("second")) {
                slotColumnIndex = 2;
            }
            else {
                return List.of();
            }
            ValueRange masterSheetRequest = sheets.spreadsheets()
                    .values()
                    .get(properties.spreadSheetId(), rangeMasterSheet)
                    .execute();
            List<List<Object>> masterSheetRows = masterSheetRequest.getValues();
            List<String> result = new ArrayList<>();
            for (List<Object> row: masterSheetRows) {
                String date = getCell(row, 0);
                String slotValue = getCell(row, slotColumnIndex);
                if ("+".equals(slotValue)) {
                    result.add(date);
                }
            }
            return result;
        } catch (IOException e) {
            log.error("Ошибка при поиске данных",e);
            return  List.of();
        }
    }

    @Override
    public void addGameToMastersList(String selectedSlot, String sheetName, String date,String userName) {
        log.info("Попробую сделать запись");
        try {
            String rowName ="";
            if (selectedSlot.equals("first")) {
                rowName = "B";
            } else {
                rowName = "C";
            }
            String findDate = sheetName+"!A2:A";
            ValueRange response = sheets.spreadsheets()
                    .values()
                    .get(properties.spreadSheetId(), findDate)
                    .execute();
            List<List<Object>> rows = response.getValues();
            Integer rowNumber =2;
            for (List<Object> row:rows) {
                String currentDate = getCell(row,0);
                if(currentDate.isBlank()) {
                    rowNumber++;
                    continue;
                }
                if (date.equals(currentDate)) {
                    break;
                }
                rowNumber++;
            }
            String range = sheetName+"!"+rowName+rowNumber+":"+rowName;
            ValueRange body = new ValueRange().setValues(List.of(
                    List.of("@"+userName)
            ));
            UpdateValuesResponse result = sheets.spreadsheets().values()
                    .update(properties.spreadSheetId(),range,body)
                    .setValueInputOption("RAW")
                    .execute();
            log.info("Обновлено ячеек: {}", result.getUpdatedCells());
        } catch (IOException e) {
            log.error("Ошибка при записи данных мастера в Google Sheets", e);
        }
    }

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
