package io.cryptomage.eidas.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.FileDocument;
import io.cryptomage.eidas.utils.FileUtilities;
import io.cryptomage.eidas.utils.XmlAttachmentsParser;

/**
 * XML file parser.
 * 
 * It's a class used to handle xml data type. It allows to: - get dss document
 * data used to validate signature, - get a list of attached files, - get a list
 * of detached files.
 * 
 * A file that is subject of signature but it is separate from signed file is
 * called a detached file by dss library.
 * 
 * @author Dariusz Napłoszek
 */
public class XmlEidasParser extends DefaultEidasParser {
	private static final Logger logger = Logger.getLogger(XmlEidasParser.class);
	private static final String XPATH_XML_DOM_URI = "//*[local-name()='Reference']/@URI";
	private static final String XPATH_XML_DOM_DESCRIPTION = "//*[local-name()='Description']";

	/**
	 * XML file parser constructor.
	 * 
	 * @param file
	 *            to be analyzed,
	 * @param rootDir
	 *            root directory of the file.
	 */
	public XmlEidasParser(File file, String rootDir) {
		super(file, rootDir);
	}

	@Override
	public DSSDocument getDSSDocumentData() {
		logger.info("Getting dss document data");
		return new FileDocument(signedFile.getAbsolutePath());
	}

	@Override
	public List<File> getAttachedFiles() {
		try {
			logger.info("Getting attached files");
			InputStream is = new FileInputStream(signedFile);
			return checkXmlAttachment(is);
		} catch (IOException e) {
			logger.error("Couldn't get attached files", e);
			return new ArrayList<>();
		}
	}

	private List<File> checkXmlAttachment(InputStream is) throws IOException {
		try {
			DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
			dFactory.setNamespaceAware(true);
			DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
			Document document = dBuilder.parse(is);

			document.normalize();
			XmlAttachmentsParser parser = new XmlAttachmentsParser(signedFile);
			return parser.checkForAttachments(document);

		} catch (ParserConfigurationException | SAXException e) {
			logger.error(e);
		}

		return new ArrayList<>();
	}

	@Override
	public List<File> getDetachedFiles(List<File> files) {
		logger.info("Getting detached files");
		List<File> ret;
		List<File> filesByXmlDomURI = getDetachedFilesByXmlDomURI();
		List<File> filesByXmlDomDescription = getDetachedFilesByXmlDomDescription();
		ret = FileUtilities.mergeFileListsWithoutDuplicates(filesByXmlDomURI, filesByXmlDomDescription);
		List<File> filesByFilename = getDetachedFilesByFilename(files);
		ret = FileUtilities.mergeFileListsWithoutDuplicates(ret, filesByFilename);
		return ret;
	}

	private List<File> getDetachedFilesByXmlDomURI() {
		List<File> detachedFiles = new ArrayList<>();
		NodeList nodeList = getNodeList(XPATH_XML_DOM_URI);
		if (nodeList == null)
			return detachedFiles;

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String fileName = node.getNodeValue();
			File detachedFile = getDetachedFile(fileName);
			if (detachedFile != null && detachedFile.exists()) {
				detachedFiles.add(detachedFile);
			}
		}
		return detachedFiles;
	}

	private List<File> getDetachedFilesByXmlDomDescription() {
		List<File> detachedFiles = new ArrayList<>();
		NodeList nodeList = getNodeList(XPATH_XML_DOM_DESCRIPTION);
		if (nodeList == null)
			return detachedFiles;

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String description = node.getTextContent();
			int index = description.lastIndexOf("filename=");
			String fileName = "";
			if (index != -1) {
				// remove filename="
				index += 10;
				// get string up to next "
				fileName = description.substring(index, description.indexOf('"', index));
			}
			File detachedFile = getDetachedFile(fileName);
			if (detachedFile == null)
				continue;
			if (detachedFile.exists()) {
				detachedFiles.add(detachedFile);
			}
		}
		return detachedFiles;
	}

	private File getDetachedFile(String fileName) {
		if (fileName == null || "".equals(fileName))
			return null;
		
		try {
			String decodedName = URLDecoder.decode(fileName, "UTF-8");
			return new File(signedFile.getParent() + "/" + decodedName);
		} catch (UnsupportedEncodingException e) {
			logger.info("Couldn't decode e: " + e + " filename: " + fileName + " skipping file...");
		}
		
		return null;
	}

	private NodeList getNodeList(String xpathString) {
		try {
			DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
			Document document = dBuilder.parse(signedFile.getAbsolutePath());

			XPathFactory xpf = XPathFactory.newInstance();
			XPath xpath = xpf.newXPath();
			return (NodeList) xpath.evaluate(xpathString, document, XPathConstants.NODESET);
		} catch (XPathExpressionException | IOException | ParserConfigurationException | SAXException e) {
			logger.error("Couldn't extract detached files from xml dom: " + e);
		}
		return null;
	}
}
