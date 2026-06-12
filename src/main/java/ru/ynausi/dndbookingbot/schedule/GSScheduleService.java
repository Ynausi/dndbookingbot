package ru.ynausi.dndbookingbot.schedule;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ynausi.dndbookingbot.configuration.GoogleSheetsProperties;
import ru.ynausi.dndbookingbot.master.Master;
import ru.ynausi.dndbookingbot.master.MasterRepository;
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
    private final MasterRepository masterRepository;
    private final MasterService masterService;
    private final Sheets sheets;
    private final GoogleSheetsProperties properties;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Map<String,Map<LocalDate,DaySchedule>> readMastersSchedule() {
        List<Master> masters = masterRepository.getMasters();
        Map<String,Map<LocalDate,DaySchedule>> result = new HashMap<>();
        for (Master master:masters) {
            Map<LocalDate,DaySchedule> masterSchedule = new TreeMap<>();
            try {
                String rangeMasterList = master.sheetName() + "!A2:C";
                        ValueRange request = sheets.spreadsheets()
                        .values()
                        .get(properties.spreadSheetId(), rangeMasterList)
                        .execute();
                List<List<Object>> rows = request.getValues();
                for (List<Object> row:rows) {
                    String dateValue = getCell(row,0).trim();
                    if (dateValue.isEmpty()) {
                        continue;
                    }
                    String firstSlotValue = getCell(row,1);
                    String secondSlotValue = getCell(row,2);
                    DaySchedule daySchedule = new DaySchedule(firstSlotValue,secondSlotValue);

                    LocalDate date = LocalDate.parse(dateValue, DATE_FORMATTER);
                    masterSchedule.put(date, daySchedule);
                }
            }   catch (IOException | DateTimeParseException e) {
                log.error("Ошибка при чтении данных из листа мастера",e);
            }
            result.put(master.masterCode(),masterSchedule);
        }
        return result;
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
