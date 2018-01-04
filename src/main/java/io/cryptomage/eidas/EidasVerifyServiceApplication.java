package io.cryptomage.eidas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Main class wrapping up eIDAS signature verification system.
 * 
 * @author Łukasz Godziejewski
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class EidasVerifyServiceApplication {
	
	/**
	 * Main method used to run eIDAS signature verification system service.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(EidasVerifyServiceApplication.class, args);
	}
}
