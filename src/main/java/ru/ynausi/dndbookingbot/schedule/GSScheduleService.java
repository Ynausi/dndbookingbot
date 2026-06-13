package ru.ynausi.dndbookingbot.schedule;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ynausi.dndbookingbot.configuration.GoogleSheetsProperties;
import ru.ynausi.dndbookingbot.master.Master;
import ru.ynausi.dndbookingbot.master.MasterCacheService;
import ru.ynausi.dndbookingbot.master.MasterService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class GSScheduleService implements GSSchedule{
    private final MasterCacheService masterCache;
    private final MasterService masterService;
    private final Sheets sheets;
    private final GoogleSheetsProperties properties;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Map<String,Map<LocalDate,DaySchedule>> readMastersSchedule() {
        List<Master> masters = masterCache.getMasters();
        Map<String,Map<LocalDate,DaySchedule>> result = new HashMap<>();
        List<String> ranges = masters.stream()
                .map(master -> "'" + master.sheetName() + "'!A2:C")
                .toList();
        try {
            BatchGetValuesResponse response = sheets.spreadsheets()
                    .values()
                    .batchGet(properties.spreadSheetId())
                    .setRanges(ranges)
                    .setMajorDimension("ROWS")
                    .setValueRenderOption("FORMATTED_VALUE")
                    .execute();
            List<ValueRange> valueRanges = response.getValueRanges();
            for (int i=0;i< masters.size();i++) {
                Master master = masters.get(i);
                Map<LocalDate, DaySchedule> masterSchedule = new TreeMap<>();
                List<List<Object>> rows = valueRanges.get(i).getValues();
                if (rows == null) {
                    result.put(master.masterCode(), masterSchedule);
                    continue;
                }
                for (List<Object> row:rows) {
                    String dateValue = getCell(row, 0).trim();

                    if (dateValue.isEmpty()) {
                        continue;
                    }
                    try {
                        String firstSlotValue = getCell(row, 1);
                        String secondSlotValue = getCell(row, 2);
                        DaySchedule daySchedule = new DaySchedule(firstSlotValue, secondSlotValue);

                        LocalDate date = LocalDate.parse(dateValue, DATE_FORMATTER);
                        masterSchedule.put(date, daySchedule);
                    } catch (DateTimeParseException e) {
                        log.warn("Не смог распарсить дату: sheetName={}, dateValue={}",
                                master.sheetName(), dateValue);
                    }
                }
                result.put(master.masterCode(),masterSchedule);
            }
        }   catch (IOException | DateTimeParseException e) {
            log.error("Ошибка при чтении данных из листа мастера",e);
        }
        return result;
    }

    public boolean updateMasterCell(String masterCode,LocalDate date,Slot slot,String userName) {
        Optional<MasterScheduleCell> cell = readCell(masterCode,date,slot);
        if (cell.isEmpty()) {
            log.warn("Ячейка не найдена(GSScheduleService)");
            return false;
        }
        Optional<Master> master = masterService.findByMasterCode(masterCode);
        if (master.isEmpty()) {
            log.warn("Мастер не найден(GSScheduleService)");
            return false;
        }
        int rowNumber = cell.get().getRowNumber();
        int columnNumber = cell.get().getColumnNumber();
        String range = master.get().sheetName() + "!" + columnIndexToLetter(columnNumber) + rowNumber;
        try {
            ValueRange body = new ValueRange().setValues(List.of(
                    List.of("@"+userName)
            ));
            log.info("Пробую обновить расписание: range={}, userName={}", range, userName);
            UpdateValuesResponse request = sheets.spreadsheets()
                    .values()
                    .update(properties.spreadSheetId(), range,body)
                    .setValueInputOption("RAW")
                    .execute();
            log.info("Обновлено ячеек: {}", request.getUpdatedCells());
            return true;
        } catch (IOException e) {
            log.error("Ошибка при записи в ячейку мастера = {}, дата = {}, слот = {},",master,date,slot);
        }
        return false;
    }

    @Override
    public boolean updateMasterSchedule(Slot selectedSlot, String masterCode, LocalDate date, String userName) {
        Optional<Master> master = masterService.findByMasterCode(masterCode);
        String column = selectedSlot == Slot.FIRST
                ? "B"
                : "C";
        if (master.isEmpty()) {
            log.warn("Мастер не найден: masterCode={}", masterCode);
            return false;
        }
        Optional<Integer> rowNumber = findRowByDate(date,master.get().sheetName());
        if (rowNumber.isEmpty()) {
            log.warn("Дата не найдена в расписании: masterCode={}, date={}", masterCode, date);
            return false;
        }
        try {
                String rangeOfReading = "'" + master.get().sheetName() +"'!" +column+rowNumber.get();
                ValueRange request = sheets.spreadsheets()
                        .values()
                        .get(properties.spreadSheetId(), rangeOfReading)
                        .execute();
                List<List<Object>> rows = request.getValues();
                String valueOfCell = getCell(rows,0,0);
                if (!"+".equals(valueOfCell)) return false;
                ValueRange body = new ValueRange().setValues(List.of(
                        List.of("@"+userName)
                ));
                log.info("Пробую обновить расписание: range={}, userName={}", rangeOfReading, userName);
                UpdateValuesResponse result = sheets.spreadsheets().values()
                        .update(properties.spreadSheetId(),rangeOfReading,body)
                        .setValueInputOption("RAW")
                        .execute();
                log.info("Обновлено ячеек: {}", result.getUpdatedCells());
                return true;
            } catch (IOException e) {
            log.error("Ошибка при чтении данных updateMasterSchedule",e );
        }
        return false;
    }

    private Optional<MasterScheduleCell> readCell(String masterCode,LocalDate date,Slot slot) {
        Optional<Master> master = masterService.findByMasterCode(masterCode);
        if (master.isEmpty()) {
            log.warn("Мастер не найден GSScheduleService");
            return Optional.empty();
        }
        String sheetName = master.get().sheetName();
        try {
            String range = sheetName + "!A1:Z";
            ValueRange response = sheets.spreadsheets()
                    .values()
                    .get(properties.spreadSheetId(), range)
                    .execute();
            List<List<Object>> rows = response.getValues();
            ///Формирую список заголовков
            List<Object> headerRows = rows.getFirst();
            Map<String,Integer> headerMap = new HashMap<>();
            int i = 0;
            for (Object header : headerRows) {
                headerMap.put(header.toString(), i);
                i++;
            }
            ///
            for (int rowIndex =1;rowIndex < rows.size(); rowIndex++) {
                List<Object> row = rows.get(rowIndex);
                if (date.format(DATE_FORMATTER).equals(getCell(row,headerMap.get("date")))) {
                    DaySchedule daySchedule = new DaySchedule(getCell(row,headerMap.get("time1(12:00-17:00)")),getCell(row,headerMap.get("time2(18:00-23:00)")));
                    if (daySchedule.isFree(slot)) {
                        int columnIndex = 3;
                        if (slot == Slot.FIRST) columnIndex=2;
                        return Optional.of(new MasterScheduleCell(true,rowIndex+1,columnIndex));
                    }
                }
            }
        } catch (IOException e) {
            log.error("Ошибка при чтении листа мастера" + sheetName + " GSSchedule",e);
        }
        return Optional.empty();
    }

    private Map<String,Integer> readHeader(String sheetName) {
        try {
            String range = sheetName + "!A1:Z";
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

        int number = columnIndex; // переводим из 0-based в 1-based

        while (number > 0) {
            int remainder = (number - 1) % 26;
            columnName.insert(0, (char) ('A' + remainder));
            number = (number - 1) / 26;
        }

        return columnName.toString();
    }

    private Optional<Integer> findRowByDate(LocalDate date,String sheetName) {
        try {
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
                if (date.format(DATE_FORMATTER).equals(currentDate)) {
                    break;
                }
                rowNumber++;
            }
            return Optional.of(rowNumber);
        } catch (IOException e) {
            log.error("Ошибка при чтении данных:",e);
        }
        return Optional.empty();
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
