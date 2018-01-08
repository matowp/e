package io.cryptomage.eidas.config;

import java.io.File;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Lists;

import io.cryptomage.eidas.service.CertificateVerifierProvider;
import io.cryptomage.eidas.service.EidasService;
import io.cryptomage.eidas.service.FileExtractor;
import io.cryptomage.eidas.service.FileValidator;
import io.cryptomage.eidas.service.TrustedProfileValidator;
import io.cryptomage.eidas.utils.FileUtilities;

/**
 * Eidas configuration class.
 * 
 * @author Dariusz Napłoszek
 */
@Configuration
public class EidasConfig {
	private static final Logger logger = Logger.getLogger(EidasConfig.class);

	@Autowired
	TrustedCertificateListsConfig trustedCertificateLists;
	
	@Value("${eidas.dssValidation.ojKeystorePass}")
	private String ojKeystorePass;

	@Value("${eidas.dssValidation.defaultConstraints}")
	private String defaultConstraints;

	@Value("${eidas.countrySpecific.PL.enableTrustedProfileValidation}")
	private String enableTrustedProfileValidation;

	@Value("${eidas.general.maxReccurenceNesting}")
	private int maxReccurenceNesting;

	@Value("${eidas.dssValidation.lotlUrl}")
	private String lotlUrl;

	@Value("${eidas.dssValidation.lotlCode}")
	private String lotlCode;
	
	@Value("${eidas.dssValidation.addDssDetailedReport}")
	private String addDssDetailedReport;
	
	@Value("${eidas.dssValidation.addDssDiagnosticReport}")
	private String addDssDiagnosticReport;
	
	@Value("${eidas.general.tempDirName}") 
	private String tempDir;
	
	@Value("${eidas.dssValidation.ojKeystoreName}") 
	private String ojKeystoreName;
	
	@Value("${eidas.dssValidation.ojKeystoreFile}") 
	private String ojKeystoreFile;
	
	private List<File> trustedCertificates;
	private File ojKeystore;

	/**
	 * eIDAS config constructor.
	 * 
	 * @param tempDir
	 * @param ojKeystoreName
	 * @param ojKeystoreFile
	 * @param trustedCertificateNames
	 * @param trustedCertificateFiles
	 */
	public EidasConfig() {
		logger.info("Creating eidas config...");
	}
	
	@Bean
	public CertificateVerifierProvider getCertificateVerifierProvider() {
		logger.info("Setting up certificate verifier provider...");
		setupFiles();
		CertificateVerifierProvider certificateVerifierProvider = new CertificateVerifierProvider();
		logger.info("Adding oj keystore: " + ojKeystore);
		
		certificateVerifierProvider.setOjKeystore(ojKeystore);
		certificateVerifierProvider.setOjKeystorePass(ojKeystorePass);
		logger.info("Adding tempDir: " + tempDir);
		certificateVerifierProvider.setTempDir(tempDir);
		logger.info("Adding lotlUrl: " + lotlUrl);
		certificateVerifierProvider.setLotlUrl(lotlUrl);
		logger.info("Adding lotlCode: " + lotlCode);
		certificateVerifierProvider.setLotlCode(lotlCode);
		certificateVerifierProvider.setTrustedCertificates(trustedCertificates);
		certificateVerifierProvider.setupCerts();

		return certificateVerifierProvider;
	}
	
	private void setupFiles() {
		File dir = new File(tempDir);
		if (!dir.exists() && !dir.mkdirs()) {
			logger.error("Couldn't create temporary folder application may not work correctly");
		}
		
		List<String> trustedCertificateFiles = trustedCertificateLists.getTrustedCertificateFiles();
		List<String> trustedCertificateNames = trustedCertificateLists.getTrustedCertificateFilesNames();
		
		trustedCertificates = Lists.newArrayList();
		if(trustedCertificateFiles.size() == trustedCertificateNames.size() 
				&& CollectionUtils.isNotEmpty(trustedCertificateNames)) {
			
			for (int i = 0; i < trustedCertificateNames.size(); i++) {
				File trustedCertificate = FileUtilities.createEncodedFile(trustedCertificateNames.get(i), trustedCertificateFiles.get(i), tempDir);
				logger.info("Adding trustedCertificates: " + trustedCertificateNames);
				trustedCertificates.add(trustedCertificate);
			}
			
		}
		ojKeystore = FileUtilities.createEncodedFile(ojKeystoreName, ojKeystoreFile, tempDir);
	}

	/**
	 * Method used to create Eidas Service
	 * 
	 * @param fileExtractor
	 * @param fileValidator
	 * @return
	 */
	@Bean(name = "eidasService")
	public EidasService getEidasService(FileExtractor fileExtractor, FileValidator fileValidator) {
		logger.info("Setting up eidas service...");
		logger.info("Default constraints: " + defaultConstraints);
		EidasService eidasService = new EidasService(tempDir, defaultConstraints);
		eidasService.setFileExtractor(fileExtractor);
		eidasService.setFileValidator(fileValidator);
		return eidasService;
	}

	@Bean
	public FileExtractor getFileExtractor() {
		logger.info("Setting up file extractor...");
		logger.info("Max reccurence nesting: " + maxReccurenceNesting);
		return new FileExtractor(maxReccurenceNesting);
	}

	/**
	 * Method used to create File Validator service
	 * 
	 * @param certificateVerifierProvider
	 * @param trustedProfileValidator
	 * @return
	 */
	@Bean
	public FileValidator getFileValidator(CertificateVerifierProvider certificateVerifierProvider,
			TrustedProfileValidator trustedProfileValidator) {
		logger.info("Setting up file validator...");
		FileValidator fileValidator = new FileValidator();
		
		fileValidator.setVerifierProvider(certificateVerifierProvider);
		logger.info("Enabled trusted profile validation: " + enableTrustedProfileValidation);
		fileValidator.setEnableTrustedProfileValidation(Boolean.parseBoolean(enableTrustedProfileValidation));
		fileValidator.setAddDssDetailedReport(Boolean.parseBoolean(addDssDetailedReport));
		fileValidator.setAddDssDiagnosticReport(Boolean.parseBoolean(addDssDiagnosticReport));
		fileValidator.setTrustedProfileValidator(trustedProfileValidator);
		return fileValidator;
	}
}
