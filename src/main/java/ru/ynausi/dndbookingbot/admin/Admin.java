package ru.ynausi.dndbookingbot.admin;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Admin {
    private String telegramUserId;
    private String chatId;
    private String adminCode;
    private String adventurePhotoId;
    private String oneShotPhotoId;
    private String companyPhotoId;
}
