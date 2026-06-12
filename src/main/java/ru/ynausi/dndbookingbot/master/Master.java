package ru.ynausi.dndbookingbot.master;

import lombok.Builder;

@Builder
public record Master(
    Integer id,
    String name,
    String masterCode,
    String description,
    String sheetName,
    Boolean active,
    Long telegramUserId,
    Long chatId,
    String photoFileId,
    String smallDescription,
    Integer rowNumber
)
{}
