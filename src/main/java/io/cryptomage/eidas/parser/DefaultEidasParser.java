package io.cryptomage.eidas.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import eu.europa.esig.dss.DSSDocument;

/**
 * Default file parser created in case if file type is not supported by eIDAS
 * service. It has fields and methods used in file parser implementations.
 * 
 * @author Dariusz Napłoszek
 */
public class DefaultEidasParser implements EidasParser {
	private static final Logger logger = Logger.getLogger(DefaultEidasParser.class);
	private static final String[] SIGNATUREEXTENSIONS = { "XAdES", "xades", "PAdES", "pades", "CAdES", "cades", "ASiC",
			"asic", "sig" };

	File signedFile;
	String rootDir;

	/**
	 * Default file parser constructor.
	 * 
	 * It assigns file and its path for further analysis.
	 * 
	 * @param file
	 *            to be analyzed,
	 * @param rootDir
	 *            root directory of the file.
	 */
	public DefaultEidasParser(File file, String rootDir) {
		logger.info("Creating " + this.getClass().toString());
		this.signedFile = file;
		this.rootDir = rootDir;
	}

	@Override
	public List<File> getDetachedFiles(List<File> files) {
		return new ArrayList<>();
	}

	@Override
	public DSSDocument getDSSDocumentData() {
		return null;
	}

	@Override
	public List<File> getAttachedFiles() {
		return new ArrayList<>();
	}

	protected List<File> getDetachedFilesByFilename(List<File> files) {
		List<File> detachedFiles = new ArrayList<>();

		if (!checkForExtension(signedFile))
			return detachedFiles;
		String filePath = signedFile.getAbsolutePath().substring(rootDir.length());
		filePath = FilenameUtils.removeExtension(filePath);
		for (File file : files) {
			if (checkForExtension(file))
				continue;
			String path = file.getAbsolutePath().substring(rootDir.length());
			if (path.contains(filePath))
				detachedFiles.add(file);
		}

		return detachedFiles;
	}

	private boolean checkForExtension(File file) {
		boolean ret = false;
		for (String extension : SIGNATUREEXTENSIONS) {
			if (file.getName().contains(extension)) {
				ret = true;
				break;
			}
		}
		return ret;
	}
}
