package io.cryptomage.eidas.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.europa.esig.dss.validation.reports.SimpleReport;
import eu.europa.esig.dss.validation.reports.wrapper.CertificateWrapper;
import eu.europa.esig.dss.validation.reports.wrapper.DiagnosticData;
import eu.europa.esig.dss.validation.reports.wrapper.TimestampWrapper;
import eu.europa.esig.dss.validation.reports.Reports;

/**
 * Class used to generate JSONObject report from dss reports object.
 * 
 * @author Dariusz Napłoszek
 */
public class ReportGenerator {
	private Reports report;
	private boolean addDssDetailedReport;
	private boolean addDssDiagnosticReport;
	private JSONObject trustedProfileValidationResult;

	/**
	 * Class constructor. Assigns private field.
	 * 
	 * @param report
	 *            Dss reports object
	 * @param addDssDetailedReport
	 * @param addDssDiagnosticReport
	 */
	public ReportGenerator(Reports report, boolean addDssDetailedReport, boolean addDssDiagnosticReport) {
		this.report = report;
		this.addDssDetailedReport = addDssDetailedReport;
		this.addDssDiagnosticReport = addDssDiagnosticReport;
	}

	/**
	 * Method used to generate simple validation report from dss reports object.
	 * 
	 * @return JSONObject with the following fields:
	 *         <ul>
	 *         <li>validation: result of validation,
	 *         <li>subvalidation: subresult of validation,
	 *         <li>signingDate,
	 *         <li>signedBy,
	 *         <li>signatureLevel,
	 *         <li>signatureFormat,
	 *         <li>signatureNumber,
	 *         <li>errors,
	 *         <li>warnings,
	 *         <li>info,
	 *         <li>(optional) detailedDssReport,
	 *         <li>(optional) diagnosticDssReport,
	 *         <li>(optional) diagnosticDssReport,
	 *		   <li>(optional) timestamp list,
	 *		   <li>subject data (RFC2253),
	 *		   <li>issuer data(RFC2253),
	 *		   <li>signature algorithm,
	 *		   <li>certificate serial number,
	 *		   <li>certificate validity period,
	 *		   <li>if certicate is destined for signing.
	 *         </ul>
	 */
	public JSONArray generateSimpleValidationReport() {
		JSONArray certificateJsonArray = new JSONArray();
		generateReport(certificateJsonArray);
		return certificateJsonArray;
	}

	private void generateReport(JSONArray certificateJsonArray) {
		SimpleReport simple = report.getSimpleReport();
		List<String> signatureIds = simple.getSignatureIdList();
		int i = 1;
		for (String signatureId : signatureIds) {
			JSONObject cert = createJsonCertificateReport(i, signatureId);
			if (trustedProfileValidationResult != null)
				cert = appendReportByTrustedProfileData(cert, simple.getInfo(signatureId));
			certificateJsonArray.put(cert);
			i++;
		}
	}

	private JSONObject createJsonCertificateReport(int i, String signatureId) {
		JSONObject cert = new JSONObject();
		SimpleReport simple = report.getSimpleReport();
		DiagnosticData diagnostic = report.getDiagnosticData();
		CertificateWrapper wrapper = diagnostic.getUsedCertificateById(diagnostic.getSigningCertificateId());
		cert.put(ReportConstantStrings.VALIDATION, simple.getIndication(signatureId));
		cert.put(ReportConstantStrings.SUBVALIDATION, simple.getSubIndication(signatureId));
		cert.put(ReportConstantStrings.SIGNING_DATE, DateFormatter.getString(simple.getSigningTime(signatureId)));
		cert.put(ReportConstantStrings.SIGNED_BY, simple.getSignedBy(signatureId));
		cert.put(ReportConstantStrings.SIGNATURE_LEVEL, simple.getSignatureLevel(signatureId));
		cert.put(ReportConstantStrings.SIGNATURE_FORMAT, simple.getSignatureFormat(signatureId));
		cert.put(ReportConstantStrings.SIGNATURE_NUMBER, i + " z " + simple.getSignaturesCount());
		cert.put(ReportConstantStrings.ERROR, getJSONArrayFromStringList(simple.getErrors(signatureId)));
		cert.put(ReportConstantStrings.WARNING, getJSONArrayFromStringList(simple.getWarnings(signatureId)));
		cert.put(ReportConstantStrings.INFO, getJSONArrayFromStringList(simple.getInfo(signatureId)));
		if(diagnostic.isThereTLevel(signatureId))
			cert.put(ReportConstantStrings.TIMESTAMP_DATA, getJSONArrayFromStringList(getTimestampData(diagnostic, signatureId)));
		cert.put(ReportConstantStrings.SUBJECT_DATA, wrapper.getCertificateDN());
		cert.put(ReportConstantStrings.ISSUER_DATA, wrapper.getCertificateIssuerDN());
		cert.put(ReportConstantStrings.SIGNATURE_ALGORITHM, getSignatureAlgorithm(diagnostic, signatureId));
		cert.put(ReportConstantStrings.CERTIFICATE_SERIAL_NUMBER, wrapper.getSerialNumber());
		cert.put(ReportConstantStrings.CERTIFICATE_VALIDITY_PERIOD, getValidityPeriod(wrapper.getNotBefore(), wrapper.getNotAfter()));
		cert.put(ReportConstantStrings.IS_FOR_SIGNING, checkIfItsForSigning(wrapper));
		if(addDssDetailedReport)
		{
			String text = report.getXmlDetailedReport();
			text = text.replace("\n", "").replace("\r", "").replace("\"", "'");
			cert.put(ReportConstantStrings.DSS_DETAILED_REPORT, text);
		}
		if(addDssDiagnosticReport)
		{
			String text = report.getXmlDiagnosticData();
			text = text.replace("\n", "").replace("\r", "").replace("\"", "'");
			cert.put(ReportConstantStrings.DSS_DIAGNOSTIC_REPORT, text);
		}
		return cert;
	}

	private List<String> getTimestampData(DiagnosticData diagnostic, String signatureId) {
		List<TimestampWrapper> timestamps = diagnostic.getTimestampList(signatureId);
		List<String> timestampStringList = new ArrayList<>();
		for (TimestampWrapper timestamp : timestamps) {
			timestampStringList.add(DateFormatter.getString(timestamp.getProductionTime()));
		}
		return timestampStringList;
	}

	private String getSignatureAlgorithm(DiagnosticData diagnostic, String signatureId) {
		String signatureEncription = diagnostic.getSignatureEncryptionAlgorithm(signatureId).getName();
		String signatureDigestAlgo = diagnostic.getSignatureDigestAlgorithm(signatureId).getName();
		return signatureDigestAlgo + " with " + signatureEncription;
	}

	private String getValidityPeriod(Date before, Date after) {
		String beforeString = DateFormatter.getString(before);
		String afterString = DateFormatter.getString(after);
		return "from " +  beforeString + " to " + afterString;
	}

	private String checkIfItsForSigning(CertificateWrapper wrapper) {
		List<String> usages = wrapper.getKeyUsages();
		for(String usage : usages) {
			if(usage.equals("nonRepudiation")) {
				return ReportConstantStrings.SIGNATURE_HAS_NON_REPUDIATION;
			}
		}
		return ReportConstantStrings.SIGNATURE_DOES_NOT_HAVE_NON_REPUDIATION;
	}

	private JSONObject appendReportByTrustedProfileData(JSONObject cert, List<String> info) {
		String validationTrustedProfile = trustedProfileValidationResult.getString(ReportConstantStrings.REPORT_RESULT);
		String infoTrustedProfile = trustedProfileValidationResult.getString(ReportConstantStrings.REPORT_INFO);
		if (ReportConstantStrings.TOTAL_PASSED.equals(validationTrustedProfile))
			cert.put(ReportConstantStrings.VALIDATION, validationTrustedProfile);

		info.add(infoTrustedProfile);
		cert.put(ReportConstantStrings.INFO, getJSONArrayFromStringList(info));
		return cert;
	}

	/**
	 * Method used to add trusted profile validation result to the report
	 * 
	 * @param trustedProfileValidationResult
	 */
	public void addTrustedProfileValidationResult(JSONObject trustedProfileValidationResult) {
		this.trustedProfileValidationResult = trustedProfileValidationResult;
	}

	private JSONArray getJSONArrayFromStringList(List<String> list) {
		JSONArray jsonArray = new JSONArray();
		for (String value : list) {
			jsonArray.put(value);
		}
		return jsonArray;
	}
}
