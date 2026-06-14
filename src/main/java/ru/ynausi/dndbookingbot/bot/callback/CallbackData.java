package ru.ynausi.dndbookingbot.bot.callback;

import ru.ynausi.dndbookingbot.schedule.Slot;

public class CallbackData {

    public static final String COMPANY = "Company";
    public static final String ONE_SHOT = "OneShot";
    public static final String MASTERS = "masters";
    public static final String BOOKING = "booking";
    public static final String CONFIRMATION = "confirmation";

    public static final String CHOSEN_MASTER_PREFIX = "Chosen master:";
    public static final String SLOT_PREFIX = "slot:";
    public static final String DATE_PREFIX = "date:";
    public static final String PHOTO_PREFIX = "Photo:";

    public static final String SLOT_FIRST = "first";
    public static final String SLOT_SECOND = "second";

    public static final String DATE_OR_TIME_MODE = "BookingModeDateOrTime:";

    public static final String MASTER_PAGE_PREFIX = "MASTER_PAGE:";
    public static final String MASTER_VIEW_PREFIX = "MASTER_VIEW:";
    public static final String IGNORE = "IGNORE";
    public static final String MASTER_BACK_TO_LIST = "MASTER_BACK_TO_LIST:";
    public static final String START_FROM_DATE_OR_TIME = "BookingStartModeTimeOrDate:";
    public static final String BACK_TO_DATES = "BackToDates";

    private CallbackData() {
    }

    public static String chosenMaster(String masterCode) {
        return CHOSEN_MASTER_PREFIX + masterCode;
    }

    public static String slot(Slot slot) {
        return SLOT_PREFIX + slot.name();
    }

    public static String date(String date) {
        return DATE_PREFIX + date;
    }

    public static String photo(String photoName) {
        return PHOTO_PREFIX + photoName;
    }

    public static String dateOrSlotMode(String dateOrSlot) {
        return DATE_OR_TIME_MODE + dateOrSlot;
    }

    public static String masterPage(int page) {
        return MASTER_PAGE_PREFIX + page;
    }

    public static String viewMaster(String masterCode, int page) {
        return MASTER_VIEW_PREFIX + masterCode + ":" + page;
    }

    public static String ignore() {
        return IGNORE;
    }

    public static String masterBackToList(int page){
        return MASTER_BACK_TO_LIST+page;
    }

    public static String startFromDateOrTimeMode(String masterOrDate) {
        return START_FROM_DATE_OR_TIME + masterOrDate;
    }
}
