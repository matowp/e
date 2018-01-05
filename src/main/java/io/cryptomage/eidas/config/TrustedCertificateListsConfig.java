package io.cryptomage.eidas.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 * Eidas configuration class for creating list of certificates.
 * 
 * @author Dariusz Napłoszek
 */
@Configuration
@ConfigurationProperties(prefix="eidas.dssValidation")
@Getter
public class TrustedCertificateListsConfig {
	private List<String> trustedCertificateFiles = new ArrayList<> ();
	private List<String> trustedCertificateFilesNames = new ArrayList<> ();
	
}
