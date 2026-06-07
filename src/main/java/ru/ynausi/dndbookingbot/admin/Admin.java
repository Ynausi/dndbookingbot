package ru.ynausi.dndbookingbot.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Admin {
    private String telegramUserId;
    private String chatId;
    private String adminCode;
    private String adventurePhotoId;
    private String oneShotPhotoId;
    private String companyPhotoId;
}
