package io.cryptomage.eidas.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger configuration class.
 * 
 * @author Dariusz Napłoszek
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
	private static final Logger logger = Logger.getLogger(SwaggerConfig.class);

	private static final String CONTACT_NAME = "Dariusz Napłoszek";
	private static final String CONTACT_URL = "";
	private static final String CONTACT_EMAIL = "dariusznaploszek@gmail.com";

	private static final String API_TITLE = "eIDAS signature validation API";
	private static final String API_DESCRIPTION = "This API allows to validate the signatures of a posted file or a package of files (zip). Response will be shown in form of a JSON.";
	private static final String API_VERSION = "1.0.0";
	private static final String API_TERMS_OF_SERVICE_URL = "";
	private static final String API_LICENSE = "";
	private static final String API_LICENSE_URL = "";

	/**
	 * Springfox bean for swagger settings.
	 * 
	 * @return
	 */
	@Bean
	public Docket swaggerSettings() {
		logger.info("Setting up swagger...");
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("io.cryptomage.eidas.controller")).paths(PathSelectors.any())
				.build().pathMapping("/").apiInfo(apiInfo()).useDefaultResponseMessages(false);
	}

	private ApiInfo apiInfo() {
		Contact contact = new Contact(CONTACT_NAME, CONTACT_URL, CONTACT_EMAIL);
		String apiVersion = getVersionNumber();
		logger.info("Api version: " + apiVersion);
		return new ApiInfo(API_TITLE, API_DESCRIPTION, apiVersion, API_TERMS_OF_SERVICE_URL, contact, API_LICENSE,
				API_LICENSE_URL);
	}

	private String getVersionNumber() {
		Properties properties = new Properties();
		try {
			properties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
			String commitIdAbbrev = String.valueOf(properties.get("git.commit.id.abbrev"));
			return commitIdAbbrev;
		} catch (IOException e) {
			logger.error("Couldn't get version number from git. Setting default value");
			return API_VERSION;
			
		}
	}
}
