package io.cryptomage.eidas.service;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import io.cryptomage.eidas.report.ReportConstantStrings;
import io.cryptomage.eidas.utils.FileUtilities;
import io.cryptomage.eidas.verifysignaturewsdl.ArrayOfAttachment;
import io.cryptomage.eidas.verifysignaturewsdl.VerifySignature;
import io.cryptomage.eidas.verifysignaturewsdl.VerifySignatureResponse;

/**
 * Service used to validate 'profil zaufany', which is a valid signature issuer
 * in Poland (Trusted Profile)
 * 
 * @author Dariusz Napłoszek
 */
public class TrustedProfileValidator extends WebServiceGatewaySupport {
	private static final Logger tpLogger = Logger.getLogger(TrustedProfileValidator.class);
	private static final String SOAP_ACTION = "verifySignature";
	private static final String VALIDATION_TAG_NAME = "ValidDocumentSignature";
	private static final String VALIDATION_ATTRIBUTE_NAME = "znaczenie";
	private static final String TRUSTED_PROFILE_TAG_NAME = "ZP";
	private static final String TRUSTED_PROFILE_ATTRIBUTE_NAME = "czy_obecny";
	private static final String TRUSTED_PROFILE_VALUE = "true";

	/**
	 * Trusted profile validator constructor
	 */
	public TrustedProfileValidator() {
		tpLogger.info("Created trusted profile validator.");
	}

	/**
	 * Method used to send a soap request to validate a trusted profile.
	 * 
	 * @param documentInByte64
	 *            document to check
	 * @param arrayOfAttachments
	 *            attachments of the document
	 * @return server response
	 */
	public JSONObject validate(byte[] documentInByte64, ArrayOfAttachment arrayOfAttachments) {
		tpLogger.info("Checking trusted profile validation");
		VerifySignature request = new VerifySignature();
		request.setDoc(documentInByte64);
		request.setAttachments(arrayOfAttachments);
		WebServiceTemplate wsTemplate = getWebServiceTemplate();
		VerifySignatureResponse response = (VerifySignatureResponse) wsTemplate.marshalSendAndReceive(request,
				new SoapActionCallback(SOAP_ACTION));
		String responseInString = response.getVerifySignatureReturn();
		return checkResponseValidationInXML(responseInString);
	}

	/**
	 * Get json validation report from given data
	 * 
	 * @param result
	 * @param info
	 * @return
	 */
	public JSONObject getJSONReport(String result, String info) {
		JSONObject json = new JSONObject();
		json.put(ReportConstantStrings.REPORT_RESULT, result);
		json.put(ReportConstantStrings.REPORT_INFO, info);
		return json;
	}

	private JSONObject checkResponseValidationInXML(String xml) {
		tpLogger.info("Checking pz.gov.pl validation response...");
		Document doc = FileUtilities.loadXMLFromString(xml);
		if (doc == null)
			return getJSONReport(ReportConstantStrings.INDETERMINATE, ReportConstantStrings.THERE_WAS_A_PROBLEM);
		String validation = getXmlElementValue(doc, VALIDATION_TAG_NAME, VALIDATION_ATTRIBUTE_NAME);
		String trustedProfile = getXmlElementValue(doc, TRUSTED_PROFILE_TAG_NAME, TRUSTED_PROFILE_ATTRIBUTE_NAME);
		return getReportFromResponse(validation, trustedProfile);
	}

	private String getXmlElementValue(Document doc, String tagName, String attributeName) {
		NodeList aList = doc.getElementsByTagName(tagName);
		String ret = "";
		for (int i = 0; i < aList.getLength(); i++) {
			Element node = (Element) aList.item(i);
			ret = node.getAttribute(attributeName);
		}
		return ret;
	}

	private JSONObject getReportFromResponse(String validation, String trustedProfile) {
		if (ReportConstantStrings.PZGOV_VALID.equals(validation)) {
			if (trustedProfile.equals(TRUSTED_PROFILE_VALUE))
				return getJSONReport(ReportConstantStrings.TOTAL_PASSED, ReportConstantStrings.VALID_WITH_PROFILE);
			else
				return getJSONReport(ReportConstantStrings.INDETERMINATE, ReportConstantStrings.VALID_WITHOUT_PROFILE);

		} else {
			if (trustedProfile.equals(TRUSTED_PROFILE_VALUE))
				return getJSONReport(ReportConstantStrings.INDETERMINATE, ReportConstantStrings.INVALID_WITH_PROFILE);
			else
				return getJSONReport(ReportConstantStrings.TOTAL_FAILED, ReportConstantStrings.INVALID_WITHOUT_PROFILE);
		}
	}

}
