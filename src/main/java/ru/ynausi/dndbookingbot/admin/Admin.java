package ru.ynausi.dndbookingbot.admin;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Admin {
    private Long telegramUserId;
    private Long chatId;
    private String adminCode;
    private String adventurePhotoId;
    private String oneShotPhotoId;
    private String companyPhotoId;
}
