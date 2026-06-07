package ru.ynausi.dndbookingbot.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.sheets")
public record GoogleSheetsProperties(
        String spreadSheetId,
        String credentialsPath
) {
}
