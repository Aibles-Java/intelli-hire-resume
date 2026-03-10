package org.aibles.intellihireresume;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IntellihireresumeApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntellihireresumeApplication.class, args);
	}

}
