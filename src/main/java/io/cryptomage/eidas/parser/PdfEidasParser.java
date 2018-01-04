package io.cryptomage.eidas.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.FileDocument;

/**
 * PDF file parser.
 * 
 * It's a class used to handle xml data type. It allows to get dss document data
 * used to validate signature.
 * 
 * @author Dariusz Napłoszek
 */
public class PdfEidasParser extends DefaultEidasParser {
	private static final Logger logger = Logger.getLogger(PdfEidasParser.class);

	/**
	 * PDF file parser constructor.
	 * 
	 * @param file
	 *            to be analyzed,
	 * @param rootDir
	 *            root directory of the file.
	 */
	public PdfEidasParser(File file, String rootDir) {
		super(file, rootDir);
	}

	@Override
	public DSSDocument getDSSDocumentData() {
		logger.info("Getting dss document data");
		return new FileDocument(signedFile.getAbsolutePath());
	}

	@Override
	public List<File> getAttachedFiles() {
		return new ArrayList<>();
	}

	@Override
	public List<File> getDetachedFiles(List<File> files) {
		return new ArrayList<>();
	}
}
