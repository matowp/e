package io.cryptomage.eidas.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.cryptomage.eidas.parser.EidasParser;
import io.cryptomage.eidas.utils.FileParserFactory;
import lombok.Setter;

/**
 * Class handling file extraction logic.
 * 
 * It uses fileParserFactory to get the file type and then gets and saves any
 * attached files. This procedure is repeated recurrently until all files are
 * checked, or a threshold is reached. Threshold value is set in properties
 * file.
 * 
 * @author Dariusz Napłoszek
 */
public class FileExtractor {
	private static final Logger logger = Logger.getLogger(FileExtractor.class);

	@Setter
	private int maxReccurenceNesting;

	/**
	 * File extractor constructor
	 * 
	 * @param maxReccurenceNesting
	 *            is a threshold for file extraction
	 */
	public FileExtractor(int maxReccurenceNesting) {
		this.maxReccurenceNesting = maxReccurenceNesting;
	}

	/**
	 * Method used to initiate file extraction.
	 * 
	 * @param topFile
	 *            First file checked for its attachments.
	 * @return List of all attached files of every file in the chain.
	 * @throws IOException
	 */
	public List<File> listAllFiles(File topFile) throws IOException {
		logger.info("Files extraction starts.");
		List<File> files = new ArrayList<>();
		files.add(topFile);
		List<File> nestedFiles = extractFileAttachments(topFile, topFile.getParent(), 0);
		if (!nestedFiles.isEmpty())
			files.addAll(nestedFiles);
		return files;
	}

	private List<File> extractFileAttachments(File file, String filesUniqueDir, int fileNesting) throws IOException {
		logger.info("File " + file.getName() + " extraction starts");
		EidasParser parser = FileParserFactory.getParserInstance(file, filesUniqueDir);
		List<File> files = new ArrayList<>();
		List<File> attachedFiles = parser.getAttachedFiles();
		if (!attachedFiles.isEmpty())
			files.addAll(attachedFiles);
		for (File attachedFile : attachedFiles) {
			if (fileNesting < maxReccurenceNesting) {
				List<File> nestedFiles = extractFileAttachments(attachedFile, filesUniqueDir, fileNesting + 1);
				if (!nestedFiles.isEmpty())
					files.addAll(nestedFiles);
			}
		}
		return files;
	}
}
