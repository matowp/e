package io.cryptomage.eidas.parser;

import java.io.File;
import java.util.List;

import eu.europa.esig.dss.DSSDocument;

/**
 * Interface for file parsers. Its methods allow user to get validation data
 * from a file in form of a DSSDocument for DSS certification validation library
 * and get any of the attached files.
 * 
 * @author Dariusz Napłoszek
 */
public interface EidasParser {
	/**
	 * Method allowing to get validation data.
	 * 
	 * @return DSSDocument object which is used by DSS library to validate file
	 *         signature.
	 */
	DSSDocument getDSSDocumentData();

	/**
	 * Method allowing to get any files attached to the analyzed file.
	 * 
	 * @return list of attached files
	 */
	List<File> getAttachedFiles();

	/**
	 * Method allowing to get detached data in order to verify detached
	 * signature type.
	 * 
	 * @param files
	 *            list of files that may hold detached files.
	 * @return detached files.
	 */
	List<File> getDetachedFiles(List<File> files);
}
