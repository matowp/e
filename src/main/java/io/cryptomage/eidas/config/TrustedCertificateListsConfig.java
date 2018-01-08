package io.cryptomage.eidas.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${eidas.dssValidation.trustedCertificateFiles}")
	private List<String> trustedCertificateFiles = new ArrayList<> ();
	
	@Value("${eidas.dssValidation.trustedCertificateFilesNames}")
	private List<String> trustedCertificateFilesNames = new ArrayList<> ();
	
}
