package io.cryptomage.eidas.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.security.wss4j.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j.support.CryptoFactoryBean;

import io.cryptomage.eidas.service.TrustedProfileValidator;
import io.cryptomage.eidas.utils.FileUtilities;

/**
 * Trusted profile configuration class.
 * 
 * @author Dariusz Napłoszek
 */
@Configuration
public class TrustedProfileConfig {
	private static final Logger logger = Logger.getLogger(TrustedProfileConfig.class);
	
	@Value("${eidas.countrySpecific.PL.trustedProfileKeystorePass}")
	private String trustedProfileKeystorePass;

	@Value("${eidas.countrySpecific.PL.trustedProfileUrl}")
	private String trustedProfileUrl;
	
	@Value("${eidas.general.tempDirName}") 
	private String tempDir;
	
	@Value("${eidas.countrySpecific.PL.trustedProfileKeystoreFile}") 
	private String trustedProfileKeystoreFile;
	
	@Value("${eidas.countrySpecific.PL.trustedProfileKeystoreName}") 
	private String trustedProfileKeystoreName;
	
	private File trustedProfileKeystore;
	/**
	 * eIDAS config constructor.
	 * 
	 * @param tempDir
	 * @param trustedCertificateName
	 * @param trustedCertificateFile
	 * @param trustedProfileKeystoreFile
	 * @param trustedProfileKeystoreName
	 */
	public TrustedProfileConfig() {
		logger.info("Creating trusted profile config...");
	}
	
	/**
	 * Security interceptor for soap messages in trusted profile validation.
	 * Used to add signature to the request
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	public Wss4jSecurityInterceptor securityInterceptor() throws Exception {
		logger.info("Creating security interceptor...");
		Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();

		// set security actions
		securityInterceptor.setSecurementActions("Signature");

		// sign the request
		setupFiles();
		String keystoreNameWithoutExtension = FilenameUtils.removeExtension(trustedProfileKeystoreName);
		securityInterceptor.setSecurementSignatureUser(keystoreNameWithoutExtension);
		securityInterceptor.setSecurementPassword(trustedProfileKeystorePass);
		securityInterceptor.setSecurementSignatureCrypto(getCryptoFactoryBean().getObject());
		securityInterceptor.setSecurementSignatureKeyIdentifier("DirectReference");
		return securityInterceptor;
	}

	private void setupFiles() {
		File dir = new File(tempDir);
		if (!dir.exists() && !dir.mkdirs()) {
			logger.error("Couldn't create temporary folder application may not work correctly");
		}
		trustedProfileKeystore = FileUtilities.createEncodedFile(trustedProfileKeystoreName, trustedProfileKeystoreFile,
				tempDir);
	}

	@Bean
	public CryptoFactoryBean getCryptoFactoryBean() throws IOException {
		logger.info("Setting up crypto factory bean...");
		CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
		cryptoFactoryBean.setKeyStorePassword(trustedProfileKeystorePass);
		if (trustedProfileKeystore != null)
			cryptoFactoryBean.setKeyStoreLocation(new ClassPathResource(trustedProfileKeystore.getPath()));
		return cryptoFactoryBean;
	}

	/**
	 * Java marshaller for java to xml conversion.
	 * 
	 * @return Jaxb2Marshaller
	 */
	@Bean
	public Jaxb2Marshaller marshaller() {
		logger.info("Creating marshaller...");
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setContextPath("io.cryptomage.eidas.verifysignaturewsdl");
		return marshaller;
	}

	/**
	 * Trusted profile validator class configuration.
	 * 
	 * @param marshaller
	 * @return Trusted profile validator object.
	 * @throws Exception
	 */
	@Bean
	public TrustedProfileValidator getTrustedProfileValidator(Jaxb2Marshaller marshaller) throws Exception {
		logger.info("Creating trusted profile validator...");
		TrustedProfileValidator trustedProfileValidator = new TrustedProfileValidator();
		trustedProfileValidator.setDefaultUri(trustedProfileUrl);
		trustedProfileValidator.setMarshaller(marshaller);
		trustedProfileValidator.setUnmarshaller(marshaller);
		ClientInterceptor[] interceptors = new ClientInterceptor[] { securityInterceptor() };
		trustedProfileValidator.setInterceptors(interceptors);
		return trustedProfileValidator;
	}
}
