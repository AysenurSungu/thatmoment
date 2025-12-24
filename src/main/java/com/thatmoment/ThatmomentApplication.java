package com.thatmoment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ThatmomentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThatmomentApplication.class, args);
	}

}
