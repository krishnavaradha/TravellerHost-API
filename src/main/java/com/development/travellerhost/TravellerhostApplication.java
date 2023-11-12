package com.development.travellerhost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RestTemplateConfig.class)
public class TravellerhostApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravellerhostApplication.class, args);
	}

}
