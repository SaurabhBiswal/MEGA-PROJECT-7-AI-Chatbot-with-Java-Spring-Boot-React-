package com.labmentix.aichatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
// Trigger deployment on Railway
public class AiChatbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiChatbotApplication.class, args);
	}

}
