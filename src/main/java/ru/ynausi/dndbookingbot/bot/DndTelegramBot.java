package ru.ynausi.dndbookingbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.photo.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.ynausi.dndbookingbot.admin.Admin;
import ru.ynausi.dndbookingbot.admin.AdminService;
import ru.ynausi.dndbookingbot.booking.BookingSession;
import ru.ynausi.dndbookingbot.booking.BookingSessionServiceImpl;
import ru.ynausi.dndbookingbot.configuration.TelegramBotProperties;
import ru.ynausi.dndbookingbot.master.Master;
import ru.ynausi.dndbookingbot.master.MasterService;
import ru.ynausi.dndbookingbot.session.AdminMasterSession;
import ru.ynausi.dndbookingbot.session.AdminMasterSessionServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class DndTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramBotProperties properties;
    private final TelegramClient telegramClient;
    private final BookingSessionServiceImpl bookingSessionService;
    private final MasterService masterService;
    private final AdminMasterSessionServiceImpl session;
    private final AdminService adminService;

    public DndTelegramBot(TelegramBotProperties properties, BookingSessionServiceImpl bookingSessionService,
                          MasterService masterService, AdminMasterSessionServiceImpl session, AdminService adminService) {
        this.properties = properties;
        this.telegramClient = new OkHttpTelegramClient(properties.token());
        this.bookingSessionService = bookingSessionService;
        this.masterService = masterService;
        this.session = session;
        this.adminService = adminService;
    }

    @Override
    public String getBotToken() {
        return properties.token();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {

        if (update.hasCallbackQuery()){
            handleCallbackQuery(update.getCallbackQuery());
            return;
        }

        if(update.hasMessage() && update.getMessage().hasPhoto()) {
            List<PhotoSize> photos = update.getMessage().getPhoto();
            PhotoSize largestPhoto = photos.get(photos.size()-1);
            String fileId = largestPhoto.getFileId();
            Long telegramUserId = update.getMessage().getFrom().getId();
            Long chatId = update.getMessage().getChatId();
            Optional<AdminMasterSession> adminMasterSession = session.findSessionByTelegramUserId(telegramUserId);
            if (adminMasterSession.isEmpty()) {
                sendMessage(chatId,"Извините пока не могу работать с картинками от пользователей");
                return;
            }
            if (adminMasterSession.get().getRole().equals("master")) {
                List<Master> masters = masterService.getMasters();
                for (Master master:masters) {
                    if (telegramUserId.toString().equals(master.telegramUserId())) {
                        Optional<Integer> rowNumber = masterService
                                .findMasterRowByTelegramId(telegramUserId,chatId);
                        if (rowNumber.isEmpty()) {
                            sendMessage(chatId,"Что-то пошло не так. Проверьте свой код мастера и начните снова");
                            return;
                        }
                        masterService.updateMasterPhoto(fileId,rowNumber.get());
                        log.info("PHOTO FILE ID: {}",fileId);
                        sendMessage(chatId,"Ваше фото было успешно обновлено");
                        return;
                    }
                }
            }
            if (adminMasterSession.get().getRole().equals("admin")) {
                adminService.updatePhoto(telegramUserId,fileId,adminMasterSession.get().getStep());
                sendMessage(chatId,"Фото успешно обновленно");
                return;
            }
            sendMessage(chatId,"Извините пока не могу работать с картинками от пользователей");
            return;
        }

        if(!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        if("/start".equals(text)) {
            firstMenu(chatId);
        }
        if (text.startsWith("/admin")) {
            String adminCode = text.substring("/admin".length()).trim();
            boolean ifAdmin = adminService.checkIfAdmin(adminCode);
            if (!ifAdmin) {
                sendMessage(chatId,"Введите верный код админа");
                return;
            }
            Long telegramUserId = update.getMessage().getFrom().getId();
            adminService.updateAdminTelegramUserIdAndChatId(telegramUserId,chatId);
            session.startSession(telegramUserId,"admin");
            sendSettingsMenu(chatId);
            return;
        }
        if(text.startsWith("/master")) {
            String masterCode = text.substring("/master".length()).trim();
            if(masterCode.isBlank()) {
                sendMessage(chatId,"После /master укажите свой код");
                return;
            }
            Long telegramUserId = update.getMessage().getFrom().getId();
            chatId = update.getMessage().getChatId();
            List<Master> masters = masterService.getMasters();
            boolean rightCode = masters.stream()
                    .map(Master::masterCode)
                    .anyMatch(masterCode::equals);
            if(!rightCode) {
                sendMessage(chatId,"Укажите верный код мастера");
                return;
            }
            Integer rowNumber = 2; //Данные начинаются с 2 ячейки
            for (Master master:masters) {
                if(master.masterCode().equals(masterCode)) {
                    session.startSession(telegramUserId,"master");
                    break;
                }
                rowNumber++;
            }
            masterService.updateMasterTelegramIdAndChatId(telegramUserId,chatId,rowNumber);
            sendMessage(chatId,"Отлично,ваши данные были обновленны. Теперь отправьте своё фото");
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long telegramUserId = callbackQuery.getFrom().getId();
        String userName = callbackQuery.getFrom().getUserName();
        if(data.startsWith("Chosen master:")) {
            startBookingSessionWithMaster(chatId,telegramUserId,userName,data);
            return;
        }
        if (data.startsWith("Company")) {
            sendMessage(chatId,"Компейн бронируется по согласованию с мастером."+"\n"
                    +"Уже отправили ему вашу ссылку. Скоро он с вами свяжется");
            return;
        }
        if (data.startsWith("OneShot")) {
            sendStartMenu(chatId);
            return;
        }
        if(data.startsWith("slot")) {
            String selectedSlot = data.substring("slot:".length());
            boolean saved = bookingSessionService.selectSlot(telegramUserId,selectedSlot);
            if(!saved) {
                sendMessage(chatId,"Бронирование устарело. Начните заново");
                return;
            }
            if (selectedSlot.equals("first")) {
                selectedSlot="12:00-17:00";
            } else {
                selectedSlot = "18:00-23:00";
            }
            sendMessage(chatId,"Вы выбрали время: " + selectedSlot);
            sendBookingDates(chatId,telegramUserId);
            return;
        }
        if (data.startsWith("date:")) {
            String selectedDate = data.substring("date:".length());
            boolean saved = bookingSessionService.selectDate(telegramUserId,selectedDate);
            if (!saved) {
                sendMessage(chatId,"Бронирование устарело. Начните заново");
                return;
            }
            sendMessage(chatId,"Вы выбрали дату: " + selectedDate);
            confirm(chatId);
            return;
        }
        if (data.startsWith("confirmation")) {
            Optional<BookingSession> bookingSession = bookingSessionService.findByTelegramUserId(telegramUserId);
            if (bookingSession.isEmpty()) {
                sendMessage(chatId,"Извините возника проблема с бронированием, попробуйте снова");
            }
            Optional<Master> master = masterService.findByMasterCode(bookingSession.get().getSelectedMasterCode());
            if (master.isEmpty()) {
                sendMessage(chatId,"Не удалось найти мастера, попробуйте снова");
                return;
            }
            masterService.addGameToMasterList(bookingSession.get().getSelectedSlot(),master.get().sheetName(),bookingSession.get().getSelectedDate(),userName);
            String selectedSlot ="";
            if (bookingSession.get().getSelectedSlot().equals("first")) {
                selectedSlot="12:00-17:00";
            } else {
                selectedSlot = "18:00-23:00";
            }
            sendMessage(chatId,"Вы успешно записались на " + selectedSlot + " " + bookingSession.get().getSelectedDate()+". \n" + "Ждём вас!");
            sendMessage(Long.valueOf(master.get().chatId()),
                    "У вас забронировали игру "
                            + bookingSession.get().getSelectedDate()
                            + " "
                            + selectedSlot
                            + " @" + userName
            );
            return;
        }
        if (data.startsWith("Photo:")) {
            String selectedPhoto = data.substring("Photo:".length());
            Optional<AdminMasterSession> adminMasterSession = session.findSessionByTelegramUserId(telegramUserId);
            if (adminMasterSession.isEmpty()) {
                sendMessage(chatId,"Пожалуйста сначала зарегиструйтесь");
                return;
            }
            adminMasterSession.get().setStep(selectedPhoto);
            sendMessage(chatId,"Отправьте фото которое вы хотите установить");
            return;

        }
        switch (data) {
            case "masters" -> sendMasters(chatId);
            case "booking" -> sendBookingDates(chatId);
            default -> sendMessage(chatId,"Неизвестная команда");
        }
    }

    private void sendSettingsMenu(Long chatId) {
        var buttonAdventureMenu = InlineKeyboardButton.builder()
                .text("Изменить картинку главного меню")
                .callbackData("Photo:adventure")
                .build();
        var buttonOneShotMenu = InlineKeyboardButton.builder()
                .text("Изменить картинку для ваншота")
                .callbackData("Photo:oneshot")
                .build();
        var buttonCompanyMenu = InlineKeyboardButton.builder()
                .text("Изменить картинку для компейна")
                .callbackData("Photo:company")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(buttonAdventureMenu),
                new InlineKeyboardRow(buttonOneShotMenu),
                new InlineKeyboardRow(buttonCompanyMenu)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Выберите какую картинку поменять")
                .replyMarkup(markup)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void firstMenu(Long chatId) {
        var buttonCampain = InlineKeyboardButton.builder()
                .text("Забронировать компейн(Долгое приключение)")
                .callbackData("Company")
                .build();
        var buttonOneShot = InlineKeyboardButton.builder()
                .text("Запронировать ваншот(Приключение на один вечер)")
                .callbackData("OneShot")
                .build();
        List<InlineKeyboardRow> keyboardRows= List.of(
                new InlineKeyboardRow(buttonOneShot),
                new InlineKeyboardRow(buttonCampain)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        Optional<Admin> admin = adminService.getAdmin();
        if (admin.get().getAdventurePhotoId().isEmpty()) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text("Привет приключенцы! Мы – интерактивный театр «Врата Героев». " +
                            "В нашем пространстве мы проводим настольно-ролевые игры. " +
                            "Хотите отправиться в приключение? Наши ведущие создадут невероятную атмосферу для вас и ваших друзей. " +
                            "Вы станете главными героями истории, разворачивающейся в мирах Dungeons and Dragons, Vampires: The Masquerade, Pathfinder и других!" +
                            "\n"+"Начать приключение!")
                    .replyMarkup(markup)
                    .build();
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
         else {
            SendPhoto photo = SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(admin.get().getAdventurePhotoId()))
                    .caption("Привет приключенцы! Мы – интерактивный театр «Врата Героев». " +
                            "В нашем пространстве мы проводим настольно-ролевые игры. " +
                            "Хотите отправиться в приключение? Наши ведущие создадут невероятную атмосферу для вас и ваших друзей. " +
                            "Вы станете главными героями истории, разворачивающейся в мирах Dungeons and Dragons, Vampires: The Masquerade, Pathfinder и других!" +
                            "\n"+"Начать приключение!")
                    .replyMarkup(markup)
                    .build();
            try {
                telegramClient.execute(photo);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
    private void sendStartMenu(Long chatId) {
        var button2 = InlineKeyboardButton.builder()
                .text("Выбор мастера")
                .callbackData("masters")
                .build();

        var button3 = InlineKeyboardButton.builder()
                .text("Выбор даты")
                .callbackData("booking")
                .build();
        List<InlineKeyboardRow> keyboardRows1 = List.of(
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows1);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Главное меню")
                .replyMarkup(markup)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void confirm(Long chatId) {
        var button = InlineKeyboardButton.builder()
                .text("Подтвердить")
                .callbackData("confirmation")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(new InlineKeyboardRow(button));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Подтвердите запись")
                .replyMarkup(markup)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendBookingDates(Long chatId) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate month = today.plusMonths(1);
    }

    private void startBookingSessionWithMaster(Long chatId,Long telegramUserId,String userName,String data) {
        String masterCode = data.substring("Chosen master:".length());
        Optional<Master> optionalMaster = masterService.findByMasterCode(masterCode);
        if (optionalMaster.isEmpty()) {
            sendMessage(chatId,"Извините, что-то пошло не так");
            return;
        }
        bookingSessionService.startSession(chatId,telegramUserId,userName,optionalMaster.get().masterCode());
        sendMessage(chatId,"Приступим к выбору времени и даты");
        sendBookingTime(chatId);
    }

    private void sendBookingTime(Long chatId) {
        var button1 = InlineKeyboardButton.builder()
                .text("12:00-17:00")
                .callbackData("slot:first")
                .build();

        var button2 = InlineKeyboardButton.builder()
                .text("18:00-23:00")
                .callbackData("slot:second")
                .build();
        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Выберите время игры")
                .replyMarkup(markup)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendBookingDates(Long chatId,Long telegramUserId) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(1);
        Optional<BookingSession> bookingSession = bookingSessionService.findByTelegramUserId(telegramUserId);
        if (bookingSession.isEmpty() ) {
            sendMessage(chatId,"Проблемы с бронированием, попробуйте снова");
            return;
        }
        Optional<Master> master = masterService.findByMasterCode(bookingSession.get().getSelectedMasterCode());
        if (master.isEmpty()) {
            sendMessage(chatId, "Мастер не найден. Попробуйте начать бронирование заново");
            return;
        }
        List<String> dates = masterService.findFreeDatesForMaster(start,end,master.get().masterCode(),bookingSession.get().getSelectedSlot());
        for (String date:dates) {
            var button = InlineKeyboardButton.builder()
                    .text(date)
                    .callbackData("date:"+date)
                    .build();
            buttons.add(button);
        }
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += 2) {
            if (i + 1 < buttons.size()) {
                keyboardRows.add(new InlineKeyboardRow(buttons.get(i), buttons.get(i + 1)));
            } else {
                keyboardRows.add(new InlineKeyboardRow(buttons.get(i)));
            }
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Выберите дату")
                .replyMarkup(markup)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMasters(Long chatId) {
        List<Master> masters = masterService.getActiveMasters();
        if (!masters.isEmpty()) {
            for(Master master:masters) {
                sendMasterCard(chatId,master);
            }
        }
    }

    private void sendMasterCard(Long chatId, Master master) {
        var button1 = InlineKeyboardButton.builder()
                .text("Выбрать")
                .callbackData("Chosen master:" + master.masterCode())
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1)
        );
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        SendPhoto photo = SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(master.photoFileId()))
                .caption(master.name()+"\n\n"+master.description())
                .replyMarkup(markup)
                .build();
        try {
            telegramClient.execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMasterChose(Long chatId){

    }

    private void sendMessage(Long chatId,String text){
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try{
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
