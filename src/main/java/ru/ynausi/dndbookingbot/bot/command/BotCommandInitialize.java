package ru.ynausi.dndbookingbot.bot.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotCommandInitialize {

    private final TelegramClient telegramClient;

    @EventListener(ApplicationReadyEvent.class)
    public void initCommands() {
        SetMyCommands setMyCommands = SetMyCommands.builder()
                .command(BotCommand.builder()
                        .command("start")
                        .description("Открыть главное меню")
                        .build())
                .command(BotCommand.builder()
                        .command("restart")
                        .description("Перезапустить бота")
                        .build())
                .command(BotCommand.builder()
                        .command("cancel")
                        .description("Отменить бронирование")
                        .build())
                .scope(BotCommandScopeDefault.builder().build())
                .build();

        try {
            telegramClient.execute(setMyCommands);
            log.info("Команды бота успешно установлены");
        } catch (TelegramApiException e) {
            log.error("Не удалось установить команды бота", e);
        }
    }
}