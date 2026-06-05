package ru.ynausi.dndbookingbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DndbookingbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DndbookingbotApplication.class, args);
	}

}
