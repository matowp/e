package io.cryptomage.eidas.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.europa.esig.dss.DSSDocument;
import io.cryptomage.eidas.verifysignaturewsdl.ArrayOfAttachment;
import io.cryptomage.eidas.verifysignaturewsdl.Attachment;

/**
 * Class used for trusted profile validation utilities.
 * 
 * @author Dariusz Napłoszek
 */
@Component
public final class TrustedProfileUtils {
	private static final Logger logger = Logger.getLogger(TrustedProfileUtils.class);

	private TrustedProfileUtils() {
	}

	/**
	 * Method to apply dssdocument to byte[] conversion.
	 * 
	 * @param document
	 * @return
	 * @throws IOException
	 */
	public static byte[] getByteFromDssDocument(DSSDocument document) throws IOException {
		logger.info("Converting dss document to byte array");
		Path filePath = Paths.get(document.getAbsolutePath());

		return Files.readAllBytes(filePath);
	}

	/**
	 * Method used to apply List of Files to ArrayOfAttachment conversion.
	 * 
	 * @param detachedFiles
	 * @return
	 * @throws IOException
	 */
	public static ArrayOfAttachment getArrayOfAttachmentsFromFileList(List<File> detachedFiles) throws IOException {
		logger.info("Converting list of file to wsdl ArrayOfAttachment");
		ArrayOfAttachment array = new ArrayOfAttachment();
		List<Attachment> list = array.getAttachment();
		for (File file : detachedFiles) {
			Attachment attachment = setupAttachment(file);
			list.add(attachment);
		}
		return array;
	}

	private static Attachment setupAttachment(File file) throws IOException {
		Attachment attachment = new Attachment();
		attachment.setName(file.getName());
		Path filePath = Paths.get(file.getAbsolutePath());
		byte[] attachmentContents = Files.readAllBytes(filePath);
		attachment.setContent(attachmentContents);
		return attachment;
	}
}
