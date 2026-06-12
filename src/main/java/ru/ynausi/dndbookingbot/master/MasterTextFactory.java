package ru.ynausi.dndbookingbot.master;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MasterTextFactory {
    public String buildMasterPageText(List<Master> masters, int page) {
        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) masters.size() / pageSize);

        List<Master> pageMasters = pageMasters(masters, page, pageSize);

        StringBuilder text = new StringBuilder();

        text.append("🧙 Выберите мастера\n\n");
        text.append("Страница ")
                .append(page + 1)
                .append(" из ")
                .append(totalPages)
                .append("\n\n");

        for (Master master : pageMasters) {
            text.append(master.name())
                    .append(" — ")
                    .append(master.smallDescription())
                    .append("\n");
        }

        text.append("\nНажмите на имя мастера ниже, чтобы открыть полную карточку.");

        return text.toString();
    }


    private List<Master> pageMasters(List<Master> masters,int page,int pageSize) {
        int totalPages = (int)Math.ceil((double) masters.size()/pageSize);
        int fromIndex = page *pageSize;
        int toIndex = Math.min(fromIndex+pageSize,masters.size());
        return masters.subList(fromIndex,toIndex);
    }
}
