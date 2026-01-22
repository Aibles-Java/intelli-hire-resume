package org.aibles.intellihireresume;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class IntellihireresumeApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntellihireresumeApplication.class, args);
	}

}
