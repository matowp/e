package io.cryptomage.eidas.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import eu.europa.esig.dss.DSSDocument;

/**
 * ZIP file parser.
 * 
 * It's a class used to handle zip data type. It allows to get a list of packed
 * files.
 * 
 * @author Dariusz Napłoszek
 */
public class ZipEidasParser extends DefaultEidasParser {
	private static final Logger logger = Logger.getLogger(ZipEidasParser.class);

	/**
	 * ZIP file parser constructor.
	 * 
	 * @param file
	 *            to be analyzed,
	 * @param rootDir
	 *            root directory of the file.
	 */
	public ZipEidasParser(File file, String rootDir) {
		super(file, rootDir);
	}

	@Override
	public DSSDocument getDSSDocumentData() {
		return null;
	}

	@Override
	public List<File> getAttachedFiles() {
		logger.info("Getting attached files");
		try {
			return unzipFile();
		} catch (IOException e) {
			logger.error("Couldn't get attached files", e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<File> getDetachedFiles(List<File> files) {
		return new ArrayList<>();
	}

	private List<File> unzipFile() throws IOException {
		List<File> fileList = new ArrayList<>();
		try (ZipFile file = new ZipFile(signedFile, Charset.forName("Cp437"))) {
			Enumeration<? extends ZipEntry> entries = file.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					File dir = new File(signedFile.getParent() + "/" + entry.getName());
					dir.mkdirs();
				} else {
					File newFile = extractZipEntry(file, entry);
					fileList.add(newFile);
				}
			}
		}
		return fileList;
	}

	private File extractZipEntry(ZipFile file, ZipEntry entry) throws IOException {
		InputStream is = file.getInputStream(entry);
		BufferedInputStream bis = new BufferedInputStream(is);
		String filePath = signedFile.getParent() + "/" + entry.getName();
		File newFile = new File(filePath);
		// In case a folder is not created in earlier zip extraction stage
		newFile.getParentFile().mkdirs();
		try (FileOutputStream fileOutput = new FileOutputStream(newFile)) {
			while (bis.available() > 0) {
				fileOutput.write(bis.read());
			}
		}

		logger.info("Unzipped " + entry.getName());
		return newFile;
	}
}
