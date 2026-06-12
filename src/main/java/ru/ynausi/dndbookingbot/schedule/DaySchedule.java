package ru.ynausi.dndbookingbot.schedule;

public record DaySchedule(
        String firstSlotValue,
        String secondSlotValue
) {
    public boolean isFree(Slot slot) {
        String value = switch (slot) {
            case FIRST -> firstSlotValue;
            case SECOND -> secondSlotValue;
        };

        return value != null && value.trim().equals("+");
    }
}
