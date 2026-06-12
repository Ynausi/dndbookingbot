package ru.ynausi.dndbookingbot.master;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.ynausi.dndbookingbot.bot.callback.CallbackData;

import java.util.ArrayList;
import java.util.List;

@Component
public class MasterKeyboardFactory {
    private static final String BACK_BUTTON ="⬅️";
    private static final String NEXT_BUTTON = "➡️";

    public InlineKeyboardMarkup buildMasterPageKeyboard(List<Master> masters, int page) {
        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) masters.size() / pageSize);
        List<Master> pageMasters = pageMasters(masters, page, pageSize);
        List<InlineKeyboardRow> rows = new ArrayList<>();
        List<InlineKeyboardButton> masterButtons = pageMasters.stream()
                .map(master -> button(
                         master.name(),
                        CallbackData.viewMaster(master.masterCode(), page)
                ))
                .toList();
        rows.addAll(splitButtonsByRows(masterButtons, 2));
        InlineKeyboardRow navigationRow = new InlineKeyboardRow();
        if (page > 0) {
            navigationRow.add(button(BACK_BUTTON + " Назад", CallbackData.masterPage(page - 1)));
        }
        navigationRow.add(button((page + 1) + "/" + totalPages, CallbackData.ignore()));
        if (page < totalPages - 1) {
            navigationRow.add(button("Вперёд " + NEXT_BUTTON, CallbackData.masterPage(page + 1)));
        }
        rows.add(navigationRow);
        return new InlineKeyboardMarkup(rows);
    }

    private List<Master> pageMasters(List<Master> masters,int page,int pageSize) {
        int totalPages = (int)Math.ceil((double) masters.size()/pageSize);
        int fromIndex = page *pageSize;
        int toIndex = Math.min(fromIndex+pageSize,masters.size());
        return masters.subList(fromIndex,toIndex);
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
    private List<InlineKeyboardRow> splitButtonsByRows(List<InlineKeyboardButton> buttons, int buttonsInRow) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (int i = 0; i < buttons.size(); i += buttonsInRow) {
            if (i + 1 < buttons.size()) {
                rows.add(new InlineKeyboardRow(buttons.get(i), buttons.get(i + 1)));
            } else {
                rows.add(new InlineKeyboardRow(buttons.get(i)));
            }
        }

        return rows;
    }
}
