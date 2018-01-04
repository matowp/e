package io.cryptomage.eidas.utils;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import io.cryptomage.eidas.parser.DefaultEidasParser;
import io.cryptomage.eidas.parser.EidasParser;
import io.cryptomage.eidas.parser.PdfEidasParser;
import io.cryptomage.eidas.parser.SigEidasParser;
import io.cryptomage.eidas.parser.XmlEidasParser;
import io.cryptomage.eidas.parser.ZipEidasParser;

/**
 * Class creating appropriate file parser objects.
 * 
 * The class has methods allowing it to recognize file type using Apache Tika
 * Library. It has only one public method used to acquire created object.
 * 
 * @author Dariusz Napłoszek
 */
public final class FileParserFactory {
	private static final Logger logger = Logger.getLogger(FileParserFactory.class);

	private static final String XML_FILE = "xml";
	private static final String ZIP_FILE = "zip";
	private static final String PDF_FILE = "pdf";
	private static final String SIG_FILE = "pkcs7-signature";

	private FileParserFactory() {
	}

	/**
	 * Method for obtaining appropriate file parser object. It needs a file to
	 * recognize its type and create appropriate file parser.
	 * 
	 * @param file
	 *            which needs to be analyzed,
	 * @param uniqueDir
	 *            root dir of the file.
	 * @return appropriate file parser.
	 */
	public static EidasParser getParserInstance(File file, String uniqueDir) throws IOException {
		logger.info("Creating file parser");
		String fileType = FileUtilities.detectFileType(file);
		switch (fileType) {
		case SIG_FILE:
			return new SigEidasParser(file, uniqueDir);
		case PDF_FILE:
			return new PdfEidasParser(file, uniqueDir);
		case XML_FILE:
			return new XmlEidasParser(file, uniqueDir);
		case ZIP_FILE:
			return new ZipEidasParser(file, uniqueDir);
		default:
			return new DefaultEidasParser(file, uniqueDir);
		}
	}
}
