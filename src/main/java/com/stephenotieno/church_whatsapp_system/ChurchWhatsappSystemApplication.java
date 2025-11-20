package com.stephenotieno.church_whatsapp_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ChurchWhatsappSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChurchWhatsappSystemApplication.class, args);
	}

}
