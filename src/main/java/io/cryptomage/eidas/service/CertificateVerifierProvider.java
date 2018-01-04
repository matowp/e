package io.cryptomage.eidas.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import eu.europa.esig.dss.client.crl.OnlineCRLSource;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.client.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.client.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.tsl.ServiceInfo;
import eu.europa.esig.dss.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.tsl.service.TSLRepository;
import eu.europa.esig.dss.tsl.service.TSLValidationJob;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.KeyStoreCertificateSource;
import lombok.Getter;
import lombok.Setter;

/**
 * This class initializes file validation sources. The validation is done by
 * external library - DSS. Local certificate storage is initialized when
 * application is started. It is refreshed once every day. Then it sets up
 * online certificate revocation status sources (CRL, OCSP), after that it sets
 * up trusted lists certificate sources.
 * 
 * Service is used to provide certificate verifier for other services.
 * 
 * @author Dariusz Napłoszek
 */
public class CertificateVerifierProvider {
	private static final Logger logger = Logger.getLogger(CertificateVerifierProvider.class);

	@Getter
	private CommonCertificateVerifier verifier;
	private TSLValidationJob job;

	@Setter
	private File ojKeystore;
	@Setter
	private String ojKeystorePass;
	@Setter
	private String tempDir; 
	@Setter
	private String lotlUrl;
	@Setter
	private String lotlCode;
	@Setter 
	private List<File> trustedCertificates;
	
	/**
	 * This constructor initializes the service by downloading and storing
	 * certificate data from provided sources. A local keystore is used for
	 * storage, which path and password are injected parameters obtained from
	 * properties file.
	 * 
	 * @param keystoreName
	 * @param keystorePass
	 * @param tempDir
	 * @param lotlUrl
	 * @param lotlCode
	 *            dir used to store request files and cache certificates
	 */
	public CertificateVerifierProvider() {
		logger.info("Setting up online certificate sources");
	}

	/**
	 * Method used to setup certificate verifier provider. 
	 */
	public void setupCerts() {
		verifier = new CommonCertificateVerifier();
		job = new TSLValidationJob();
		
		setupCache();
		setupSources();
		job.setLotlUrl(lotlUrl);
		job.setLotlCode(lotlCode);
		job.refresh();
	}

	/**
	 * Scheduled task for refreshing certificates. By default it is invoked once
	 * every day. Scheduler configuration is stored in properties file.
	 */
	@Scheduled(cron = "${eidas.scheduler.cron}")
	public void refreshCerts() {
		logger.info("Running certificate refresh utility");
		job.refresh();
	}

	private void setupCache() {
		logger.info("Setting up cache for certificate storage");
		File cacheFolder = new File(tempDir);
		if (!cacheFolder.exists() && !cacheFolder.mkdirs()) {
			logger.error("Couldn't create folder " + tempDir + " for certificate storage");
		}

		FileCacheDataLoader fileCacheDataLoader = new FileCacheDataLoader();
		fileCacheDataLoader.setFileCacheDirectory(cacheFolder);
		verifier.setDataLoader(fileCacheDataLoader);
	}

	private void setupSources() {
		logger.info("Setting up certificate sources");

		setupDataLoader();
		TrustedListsCertificateSource certificateSource = setupCertificateSources();
		setupTSLRepository(certificateSource);
	}

	private void setupTSLRepository(TrustedListsCertificateSource certificateSource) {
		TSLRepository tslRepository = new TSLRepository();
		tslRepository.setTrustedListsCertificateSource(certificateSource);
		job.setRepository(tslRepository);
	}

	private void setupDataLoader() {
		CommonsDataLoader commonsDataLoader = new CommonsDataLoader();
		setupRevocationSources(commonsDataLoader);
		job.setDataLoader(commonsDataLoader);
	}

	private TrustedListsCertificateSource setupCertificateSources() {
		KeyStoreCertificateSource keyStoreCertificateSource = new KeyStoreCertificateSource(ojKeystore,
				"PKCS12", ojKeystorePass);
		job.setDssKeyStore(keyStoreCertificateSource);

		TrustedListsCertificateSource certificateSource = new TrustedListsCertificateSource();
		addCertificateToSource(certificateSource);
		verifier.setTrustedCertSource(certificateSource);
		return certificateSource;
	}
	
	private void addCertificateToSource(TrustedListsCertificateSource certificateSource) {
		if (trustedCertificates == null)
			return;
	
		ServiceInfo serviceInfo = new ServiceInfo();
		for (File trustedCertificate : trustedCertificates) {
			try (InputStream in = new FileInputStream(trustedCertificate)){
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
				CertificateToken token = new CertificateToken(cert);
				logger.info("Added trusted certificate to dss: " + trustedCertificate);
				certificateSource.addCertificate(token, serviceInfo);
			} catch (CertificateException | IOException e) {
				logger.info("There was a problem with adding trusted certificate: " + e);
			}
		}
	}
	
	private void setupRevocationSources(CommonsDataLoader commonsDataLoader) {
		OnlineCRLSource crlSource = new OnlineCRLSource(commonsDataLoader);
		verifier.setCrlSource(crlSource);

		OnlineOCSPSource ocspSource = new OnlineOCSPSource();
		verifier.setOcspSource(ocspSource);
	}
}
