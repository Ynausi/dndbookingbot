package ru.ynausi.dndbookingbot.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MasterScheduleCell {
    private boolean isFree;
    private int rowNumber;
    private int columnNumber;
}
