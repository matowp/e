package io.cryptomage.eidas.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class implementing file utilities.
 * 
 * @author Dariusz Napłoszek
 */
@Component
public final class FileUtilities {
	private static final Logger logger = Logger.getLogger(FileUtilities.class);

	private FileUtilities() {
	}

	/**
	 * Method used to detect file type.
	 * 
	 * @param file
	 *            to check
	 * @return file type
	 * @throws IOException
	 */
	public static String detectFileType(File file) throws IOException {
		logger.info("Detecting file type");
		Detector detector = new DefaultDetector(MimeTypes.getDefaultMimeTypes());
		final Metadata metadata = new Metadata();

		TikaInputStream tikaIS = null;
		try {
			tikaIS = TikaInputStream.get(file);
			return detector.detect(tikaIS, metadata).getSubtype();
		} finally {
			if (tikaIS != null) {
				tikaIS.close();
			}
		}
	}

	/**
	 * Method used to create a directory with unique name.
	 * 
	 * @param rootDir
	 *            directory in which unique dir is to be created.
	 * @return File object of the unique directory.
	 * @throws IOException
	 */
	public static File createUniqueDir(String rootDir) throws IOException {
		String uniqueDir = UUID.randomUUID().toString();
		File dir = new File(rootDir + "/" + uniqueDir);

		if (!dir.mkdirs()) {
			String errorMsg = "Couldn't create temporary unique folder";
			logger.error(errorMsg);
			throw new IOException(errorMsg);
		}
		return dir;
	}

	/**
	 * Method used to convert multipart file to a file.
	 * 
	 * @param dir
	 *            Directory in which file will be created.
	 * @param multipartFile
	 *            Object to be converted.
	 * @return File object from multipart file.
	 * @throws IOException
	 */
	public static File createFileFromMultipartFile(String dir, MultipartFile multipartFile) throws IOException {
		if (null == multipartFile || multipartFile.isEmpty())
			return new File(dir + "emptyConstraints");
		File file = new File(dir + multipartFile.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(multipartFile.getBytes());
		fos.close();
		if (!file.isFile()) {
			String errorMsg = "Couldn't create file from multipart file";
			logger.error(errorMsg);
			throw new IOException(errorMsg);
		}
		return file;
	}

	/**
	 * Method used to merge two lists of files.
	 * 
	 * @param first
	 *            list of files
	 * @param second
	 *            list of files
	 * @return A list that has all elements of first list and those from second
	 *         list that are not duplicates.
	 */
	public static List<File> mergeFileListsWithoutDuplicates(List<File> first, List<File> second) {
		List<File> ret = new ArrayList<>();
		ret.addAll(first);
		for (File file : second) {
			if (!ret.contains(file))
				ret.add(file);
		}
		return ret;
	}

	/**
	 * Method used to check if xml file is valid.
	 * 
	 * @param xmlFile
	 *            to be checked
	 * @return whether a file is valid or not
	 */
	public static boolean checkIfXmlIsValid(File xmlFile) {
		DocumentBuilder dBuilder = getDocumentBuilder();
		if (dBuilder == null)
			return false;

		try {
			dBuilder.parse(xmlFile);
		} catch (SAXException | IOException e) {
			logger.info("This is not a valid xml file,  " + e);
			return false;
		}
		return true;
	}

	private static DocumentBuilder getDocumentBuilder() {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			logger.error("There was a problem with document builder,  " + e1);
		}
		return dBuilder;
	}

	/**
	 * Method used to load xml from string expression
	 * 
	 * @param xml
	 *            string
	 * @return xml Document
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document loadXMLFromString(String xml) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		InputSource is = new InputSource(new StringReader(xml));
		try {
			builder = factory.newDocumentBuilder();
			return builder.parse(is);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			logger.info("Couldn't load xml from string: " + e);
		}
		return null;
	}
	
	/**
	 * Method used to decode file encoded in base64 and create its instance in the supplied folder
	 * @param fileName Name of the file to create
	 * @param fileData File data in base64
	 * @param dir Directory to create file
	 * @return
	 */
	public static File createEncodedFile(String fileName, String fileData, String dir) {
		File file = new File(dir + "/" + fileName);
		byte[] data = Base64.getDecoder().decode(fileData);
		try (OutputStream stream = new FileOutputStream(file)) {
			stream.write(data);
		} catch (IOException e) {
			logger.error("Couldn't create file: " + fileName + " error: " + e);
			return null;
		}
		return file;
	}
}
