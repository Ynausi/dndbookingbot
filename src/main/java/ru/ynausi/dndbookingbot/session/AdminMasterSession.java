package ru.ynausi.dndbookingbot.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminMasterSession {
    private Long telegramUserId;
    private String code;
    private String role;
    private String step;
}
