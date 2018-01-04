package io.cryptomage.eidas.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.web.multipart.MultipartFile;

import io.cryptomage.eidas.utils.FileUtilities;
import lombok.Setter;

/**
 * Main service used to pass requests from controller to appropriate services.
 * 
 * @author Łukasz Godziejewski and Dariusz Napłoszek
 */
public class EidasService {
	private static final Logger logger = Logger.getLogger(EidasService.class);

	@Setter
	private FileExtractor fileExtractor;

	@Setter
	private FileValidator fileValidator;

	@Setter
	private String tempDir;

	@Setter
	private String localValidationPolicyPath;

	/**
	 * eIDAS service constructor.
	 * 
	 * @param tempDir
	 *            dir used to store request files and cache certificates
	 * @param localValidationPolicyPath
	 *            path to local validation file
	 */
	public EidasService(String tempDir, String localValidationPolicyPath) {
		logger.info("Creating eidas service.");
		this.tempDir = tempDir;
		this.localValidationPolicyPath = localValidationPolicyPath;
	}

	/**
	 * Method for validating any file.
	 * 
	 * The obtained file is stored in an unique directory and then checked for
	 * attachments. Any attachments are also stored in the directory and the
	 * procedure is repeated for attached files. Then all files are validated
	 * and the results are stored in a JSON array. The result should be an array
	 * of file names/paths and their signature status.
	 * 
	 * @param validationFile
	 *            File sent by the API consumer.
	 * @param policy
	 *            Validation policy file.
	 * @return Signature validation result as a string.
	 * @throws IOException
	 *             thrown if file is not readable.
	 */
	public String validate(MultipartFile validationFile, MultipartFile policy) throws IOException {
		logger.info("Got file validation request, process started.");
		if (validationFile == null || validationFile.isEmpty())
			throw new IOException();

		File dir = FileUtilities.createUniqueDir(tempDir);
		File topFile = FileUtilities.createFileFromMultipartFile(dir.getAbsolutePath() + "/", validationFile);
		List<File> files = fileExtractor.listAllFiles(topFile);
		File policyFile = getPolicyFile(policy, dir);
		JSONArray reportJsArray = fileValidator.validateFiles(dir.getAbsolutePath(), files, policyFile);
		FileUtils.deleteDirectory(dir);

		logger.info("Validation done. Sending report.");
		return reportJsArray.toString();
	}

	private File getPolicyFile(MultipartFile policy, File dir) throws IOException {
		if (policy == null || policy.isEmpty()) {
			return new File(localValidationPolicyPath);
		}

		File policyFile = FileUtilities.createFileFromMultipartFile(dir.getAbsolutePath() + "/", policy);
		if (FileUtilities.checkIfXmlIsValid(policyFile)) {
			return policyFile;
		}

		return new File(localValidationPolicyPath);
	}

}
