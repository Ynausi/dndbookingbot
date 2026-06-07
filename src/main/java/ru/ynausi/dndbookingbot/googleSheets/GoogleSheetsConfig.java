package ru.ynausi.dndbookingbot.googleSheets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import ru.ynausi.dndbookingbot.configuration.GoogleSheetsProperties;

import java.io.InputStream;
import java.util.List;

@Configuration
public class GoogleSheetsConfig {

    @Bean
    public Sheets sheets(
            GoogleSheetsProperties properties,
            ResourceLoader resourceLoader
    ) throws Exception {
        Resource resource = resourceLoader.getResource(properties.credentialsPath());

        try (InputStream inputStream = resource.getInputStream()) {
            GoogleCredentials credentials = ServiceAccountCredentials
                    .fromStream(inputStream)
                    .createScoped(List.of(SheetsScopes.SPREADSHEETS));


            return new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials)
            )
                    .setApplicationName("Dnd Booking Bot")
                    .build();
        }
    }
}
