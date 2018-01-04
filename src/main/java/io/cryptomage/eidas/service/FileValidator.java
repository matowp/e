package io.cryptomage.eidas.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import io.cryptomage.eidas.parser.EidasParser;
import io.cryptomage.eidas.report.ReportConstantStrings;
import io.cryptomage.eidas.report.ReportGenerator;
import io.cryptomage.eidas.utils.FileParserFactory;
import io.cryptomage.eidas.utils.FileUtilities;
import io.cryptomage.eidas.utils.TrustedProfileUtils;
import io.cryptomage.eidas.verifysignaturewsdl.ArrayOfAttachment;
import lombok.Setter;

/**
 * Class handling file validation logic.
 * 
 * It uses fileParserFactory to get the file type and proceeds to validate it.
 * If a file has any detached data it will try to get it and add to dssDocument
 * object. The procedure is repeated until all files are validated.
 * 
 * A file that is subject of signature but it is separate from signed file is
 * called a detached file by dss library.
 * 
 * @author Dariusz Napłoszek
 */
public class FileValidator {
	private static final Logger logger = Logger.getLogger(FileValidator.class);

	@Setter
	private CertificateVerifierProvider verifierProvider;

	@Setter
	private TrustedProfileValidator trustedProfileValidator;

	@Setter
	private boolean enableTrustedProfileValidation;

	@Setter
	private boolean addDssDetailedReport;
	
	@Setter 
	private boolean addDssDiagnosticReport;
	
	
	/**
	 * Main method used to initiate files validation. The procedure is described
	 * in class documentation.
	 * 
	 * @param filesUniqueDir
	 *            Root dir of the files
	 * @param files
	 *            List of files to be validated
	 * @param policyFile
	 *            File with validation constraints
	 * @return validation report
	 * @throws IOException
	 */
	public JSONArray validateFiles(String filesUniqueDir, List<File> files, File policyFile) throws IOException {
		logger.info("Files validation starts");
		JSONArray jsArray = new JSONArray();
		for (File file : files) {
			String filename = file.getName();

			logger.info("File " + filename + " validation starts");
			JSONObject fileJson = new JSONObject();
			EidasParser parser = FileParserFactory.getParserInstance(file, filesUniqueDir);
			List<File> detachedFiles = parser.getDetachedFiles(files);
			fileJson.put(ReportConstantStrings.FILE_NAME, filename);
			if (!detachedFiles.isEmpty()) {
				fileJson.put(ReportConstantStrings.DETACHED_FILES, getDetachedFilesJSONArray(filesUniqueDir, detachedFiles));
			}
			DSSDocument doc = parser.getDSSDocumentData();
			if (doc != null) {
				logger.info("Checking validation");
				fileJson.put(ReportConstantStrings.CERTIFICATE_DATA, validateSignature(doc, policyFile, detachedFiles));
			}
			jsArray.put(fileJson);
		}
		return jsArray;
	}

	private JSONArray getDetachedFilesJSONArray(String filesUniqueDir, List<File> detachedFiles) {
		JSONArray ret = new JSONArray();
		for (int i = 0; i < detachedFiles.size(); i++) {
			String detachedFileName = detachedFiles.get(i).getAbsolutePath();
			if (detachedFileName.length() >= filesUniqueDir.length() + 1)
				ret.put(detachedFileName.substring(filesUniqueDir.length() + 1));
		}
		return ret;
	}

	private JSONArray validateSignature(DSSDocument document, File policy, List<File> detachedFiles) {
		SignedDocumentValidator validator = SignedDocumentValidator.fromDocument(document);
		validator.setCertificateVerifier(verifierProvider.getVerifier());

		setDetachedContents(validator, detachedFiles);

		ReportGenerator reportGenerator;
		reportGenerator = new ReportGenerator(validator.validateDocument(policy), addDssDetailedReport, addDssDiagnosticReport);
		if (enableTrustedProfileValidation)
			reportGenerator.addTrustedProfileValidationResult(validateTrustedProfile(document, detachedFiles));

		return reportGenerator.generateSimpleValidationReport();
	}

	private JSONObject validateTrustedProfile(DSSDocument document, List<File> detachedFiles) {
		logger.info("Trying to check for trusted profile data.");
		File fileToCheck = new File(document.getAbsolutePath());
		if (!checkIfShouldValidateTrustedProfile(fileToCheck))
			return trustedProfileValidator.getJSONReport(ReportConstantStrings.INDETERMINATE, ReportConstantStrings.NOT_AN_XML);

		try {
			byte[] fileToValidate = TrustedProfileUtils.getByteFromDssDocument(document);
			ArrayOfAttachment attachments = TrustedProfileUtils.getArrayOfAttachmentsFromFileList(detachedFiles);
			return trustedProfileValidator.validate(fileToValidate, attachments);
		} catch (Exception e) {
			logger.error("There was a problem with generating trusted profile validation data: " + e);
		}
		return trustedProfileValidator.getJSONReport(ReportConstantStrings.INDETERMINATE,
				ReportConstantStrings.TRUSTED_PROFILE_NOT_SUPPORTED_OR_ERROR);
	}

	private boolean checkIfShouldValidateTrustedProfile(File fileToCheck) {
		String fileType = "";
		try {
			fileType = FileUtilities.detectFileType(fileToCheck);
		} catch (IOException e1) {
			logger.error("There was a problem with detecting file type: " + e1);
		}
		if (("xml").equals(fileType))
			return true;
		return false;
	}

	private void setDetachedContents(SignedDocumentValidator validator, List<File> detachedFiles) {
		if (detachedFiles.isEmpty())
			return;

		List<DSSDocument> detachedContentsList = new ArrayList<>();
		for (File file : detachedFiles) {
			DSSDocument detachedContents = new FileDocument(file.getAbsolutePath());
			detachedContentsList.add(detachedContents);
		}
		validator.setDetachedContents(detachedContentsList);
	}
}
