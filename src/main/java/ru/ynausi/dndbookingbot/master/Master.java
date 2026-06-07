package ru.ynausi.dndbookingbot.master;

public record Master(
    String id,
    String name,
    String masterCode,
    String description,
    String sheetName,
    String active,
    String telegramUserId,
    String chatId,
    String photoFileId
)
{}
