package io.cryptomage.eidas.report;

/**
 * Class used to centralize all string variables from generated reports
 * 
 * @author Dariusz Napłoszek
 */
public final class ReportConstantStrings {
	public static final String FILE_NAME = "fileName";
	public static final String DETACHED_FILES = "detachedFiles";
	public static final String CERTIFICATE_DATA = "certificateData";
	
	// Validation report
	public static final String VALIDATION = "validation";
	public static final String SUBVALIDATION = "subvalidation";
	public static final String SIGNING_DATE = "signingDate";
	public static final String SIGNED_BY = "signedBy";
	public static final String SIGNATURE_LEVEL = "signatureLevel";
	public static final String SIGNATURE_FORMAT = "signatureFormat";
	public static final String SIGNATURE_NUMBER = "signatureNumber";
	public static final String ERROR = "errors";
	public static final String WARNING = "warnings";
	public static final String INFO = "info";
	public static final String TIMESTAMP_DATA = "timestamp";
	public static final String SUBJECT_DATA = "subjectDistinguishedName";
	public static final String ISSUER_DATA = "issuerDistinguishedName";
	public static final String SIGNATURE_ALGORITHM = "signatureAlgorithm";
	public static final String CERTIFICATE_SERIAL_NUMBER = "certificateSerialNumber";
	public static final String CERTIFICATE_VALIDITY_PERIOD = "certificateValidityPeriod";
	public static final String IS_FOR_SIGNING = "isForSigning";

	public static final String SIGNATURE_HAS_NON_REPUDIATION = "Yes";
	public static final String SIGNATURE_DOES_NOT_HAVE_NON_REPUDIATION = "No";
	
	public static final String DSS_DETAILED_REPORT = "dssDetailedReport";
	public static final String DSS_DIAGNOSTIC_REPORT = "dssDiagnosticReport";

	public static final String TOTAL_PASSED = "TOTAL_PASSED";
	public static final String INDETERMINATE = "INDETERMINATE";
	public static final String TOTAL_FAILED = "TOTAL_FAILED";

	// File Validator
	public static final String NOT_AN_XML = "Not an xml - did not check for trusted profile.";
	public static final String TRUSTED_PROFILE_NOT_SUPPORTED_OR_ERROR = "Trusted profile validation not supported, or there was a problem with trusted profile validation.";

	// Trusted Profile Validator
	public static final String PZGOV_VALID = "Prawidłowy";
	public static final String VALID_WITH_PROFILE = "Trusted profile validation: Valid signature with trusted profile.";
	public static final String INVALID_WITH_PROFILE = "Trusted profile validation: Signature is invalid, but it has a valid trusted profile.";
	public static final String VALID_WITHOUT_PROFILE = "Trusted profile validation: Valid signature without trusted profile.";
	public static final String INVALID_WITHOUT_PROFILE = "Trusted profile validation: Signature is invalid, and it doesn't have a valid trusted profile.";
	public static final String THERE_WAS_A_PROBLEM = "Trusted profile validation: There was a problem with reading xml response from pz.gov.pl service.";

	public static final String REPORT_RESULT = "result";
	public static final String REPORT_INFO = "info";

	private ReportConstantStrings() {
	}
}
