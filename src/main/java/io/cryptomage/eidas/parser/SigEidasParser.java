package io.cryptomage.eidas.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DSSException;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import io.cryptomage.eidas.utils.FileUtilities;

/**
 * Sig file parser.
 * 
 * It's a class used to handle sig data type. It allows to: - get dss document
 * data used to validate signature, - get a list of detached files.
 * 
 * A file that is subject of signature but it is separate from signed file is
 * called a detached file by dss library.
 * 
 * @author Dariusz Napłoszek
 */
public class SigEidasParser extends DefaultEidasParser {
	private static final Logger logger = Logger.getLogger(SigEidasParser.class);

	/**
	 * Sig file parser constructor.
	 * 
	 * @param file
	 *            to be analyzed,
	 * @param rootDir
	 *            root directory of the file.
	 */
	public SigEidasParser(File file, String rootDir) {
		super(file, rootDir);
	}

	@Override
	public DSSDocument getDSSDocumentData() {
		logger.info("Getting dss document data");
		return new FileDocument(signedFile.getAbsolutePath());
	}

	@Override
	public List<File> getAttachedFiles() {
		logger.info("Getting attached files");
		ArrayList<File> list = new ArrayList<>();
		File attachedFile = createAttachedFile();
		if (attachedFile.exists())
			list.add(attachedFile);
		return list;
	}

	@Override
	public List<File> getDetachedFiles(List<File> files) {
		logger.info("Getting detached files");
		List<File> filesByFileContents = getDetachedFilesByFileContents();
		List<File> filesByFileName = getDetachedFilesByFilename(files);
		return FileUtilities.mergeFileListsWithoutDuplicates(filesByFileContents, filesByFileName);
	}

	public List<File> getDetachedFilesByFileContents() {
		List<File> detachedFiles = new ArrayList<>();
		try {
			String fileName = getFileNameFromContents();
			File detachedFile = new File(signedFile.getParent() + "/" + fileName);
			if (detachedFile.exists()) {
				detachedFiles.add(detachedFile);
			}
		} catch (IOException e) {
			logger.error("Couldn't extract detached files from file contents: " + e);
		}
		return detachedFiles;
	}

	private File createAttachedFile() {
		File attachedFile = new File(
				signedFile.getParent() + "/" + FilenameUtils.removeExtension(signedFile.getName()));
		SignedDocumentValidator validator = SignedDocumentValidator
				.fromDocument(new FileDocument(signedFile.getAbsolutePath()));
		validator.setCertificateVerifier(new CommonCertificateVerifier());
		try {
			
			String firstSignatureId = validator.validateDocument().getSimpleReport().getFirstSignatureId();
			List<DSSDocument> originalDocuments = validator.getOriginalDocuments(firstSignatureId);
			DSSDocument originalFile = null;
			
			if(CollectionUtils.isNotEmpty(originalDocuments)) {
				originalFile = originalDocuments.get(0);
			}
			
			if (originalFile != null)
				originalFile.save(attachedFile.getAbsolutePath());

		} catch (DSSException | IOException e) {
			logger.error("Couldn't extract attached files from file contents: " + e);
		}
		return attachedFile;
	}

	private String getFileNameFromContents() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(signedFile));
		String line;
		String fileName = "Wrongfilename";
		while ((line = br.readLine()) != null) {
			int index = line.lastIndexOf("filename=");
			if (index != -1) {
				index += 10;
				fileName = line.substring(index, line.indexOf('"', index));
				br.close();
				return fileName;
			}
		}
		br.close();
		return fileName;
	}
}
