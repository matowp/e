/**
 * 
 */
package io.cryptomage.eidas.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class used to parse xml files for attachments.
 * 
 * @author Dariusz Napłoszek
 */
public class XmlAttachmentsParser {
	private static final Logger logger = Logger.getLogger(XmlAttachmentsParser.class);
	private static final String CRD_GOV_NAMESPACE_1 = "http://crd.gov.pl/wzor/2013/01/21/1086/";
	private static final String CRD_GOV_NAMESPACE_2 = "http://crd.gov.pl/wzor/2014/02/03/1495/";
	private static final String CRD_GOV_STRUCT = "http://crd.gov.pl/xml/schematy/struktura/";
	private static final String W3_NAMESPACE = "http://www.w3.org/2000/09/xmldsig#";
	private static final String W3_ENCODING = "http://www.w3.org/2000/09/xmldsig#base64";
	private static final String ENCODING = "Encoding";
	private static final String ENCODING_PL = "kodowanie";
	private static final String FILENAME_ATTRIBUTE = "nazwaPliku";
	private static final String BASE64 = "base64";
	private static final String TAGNAME_MD5 = "md5";
	private static final String TAGNAME_ATTACHMENT_DATA = "DaneZalacznika";
	private static final String TAGNAME_OBJECT = "Object";

	private File signedFile;

	/**
	 * Class constructor assigns private fields.
	 * 
	 * @param savePath
	 *            path to save parsed attachments,
	 * @param signedFile
	 *            file to check for attachments
	 */
	public XmlAttachmentsParser(File signedFile) {
		this.signedFile = signedFile;
	}

	/**
	 * Method used to initiate checking for attachments in xml file. It will
	 * check the namespace of the file and then apply appropriate function to
	 * handle file attachment extraction.
	 * 
	 * @param document
	 *            to check for attachments
	 * @return list of files attached to the xml file
	 * @throws IOException
	 */
	public List<File> checkForAttachments(Document document) throws IOException {
		List<File> fileList;

		String namespace = document.getDocumentElement().getNamespaceURI();
		if (namespace == null)
			namespace = "";
		switch (namespace) {
		case CRD_GOV_NAMESPACE_1:
		case CRD_GOV_NAMESPACE_2:
			fileList = checkForAttachmentsInCrdGovNamespace(document);
			break;
		case W3_NAMESPACE:
			fileList = checkForAttachmentsInW3Namespace(document);
			break;
		default:
			fileList = checkForAttachmentsInOtherNamespace(document);
		}
		return fileList;
	}

	private List<File> checkForAttachmentsInCrdGovNamespace(Document document) throws IOException {
		List<File> fileList = new ArrayList<>();

		NodeList aList = document.getElementsByTagNameNS("*", TAGNAME_MD5);
		for (int i = 0; i < aList.getLength(); i++) {
			Node node = aList.item(i);
			byte[] buffer = Base64.getDecoder().decode(node.getTextContent().getBytes());
			File attachment = parseAttachment(buffer, null, i);
			if (attachment.isFile())
				fileList.add(attachment);
		}
		return fileList;
	}

	private List<File> checkForAttachmentsInOtherNamespace(Document document) throws IOException {
		List<File> fileList = new ArrayList<>();

		NodeList aList = document.getElementsByTagNameNS("*", TAGNAME_ATTACHMENT_DATA);
		for (int i = 0; i < aList.getLength(); i++) {
			Node node = aList.item(i);
			if (node.getNamespaceURI().startsWith(CRD_GOV_STRUCT)) {
				Element parent = (Element) node.getParentNode();
				if (!BASE64.equals(parent.getAttribute(ENCODING_PL))) {
					continue;
				}
				byte[] buffer = Base64.getDecoder().decode(node.getTextContent().trim().getBytes());
				String fileName = parent.getAttribute(FILENAME_ATTRIBUTE);
				File attachment = parseAttachment(buffer, fileName, 0);
				if (attachment.isFile())
					fileList.add(attachment);
			}
		}
		return fileList;
	}

	private List<File> checkForAttachmentsInW3Namespace(Document document) throws IOException {
		List<File> fileList = new ArrayList<>();

		NodeList aList = document.getElementsByTagNameNS("*", TAGNAME_OBJECT);
		for (int i = 0; i < aList.getLength(); i++) {
			Node node = aList.item(i);
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null && attributes.getNamedItem(ENCODING) != null
					&& W3_ENCODING.equals(attributes.getNamedItem(ENCODING).getTextContent())) {
				byte[] buffer = Base64.getDecoder().decode(node.getTextContent().getBytes());

				File attachment = parseAttachment(buffer, null, i);
				if (attachment.isFile())
					fileList.add(attachment);
			}
		}
		return fileList;
	}

	private File parseAttachment(byte[] buffer, String fileName, int attachmentNumber) throws IOException {
		String filePath;
		if (fileName == null)
			filePath = signedFile.getParent() + "/" + "Zal" + attachmentNumber
					+ FilenameUtils.removeExtension(signedFile.getName());
		else
			filePath = signedFile.getParent() + "/" + fileName;
		File attachedFile = new File(filePath);
		FileOutputStream fos = new FileOutputStream(attachedFile);
		fos.write(buffer);
		fos.close();
		logger.info("File created in " + attachedFile.getAbsolutePath());
		return attachedFile;
	}
}
