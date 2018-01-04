package io.cryptomage.eidas.swagger;

/**
 * Class used to store swagger descriptions
 * 
 * @author Dariusz Napłoszek
 */
public final class SwaggerAnnotations {
	public static final String VALIDATE_PATH_SUMMARY = "Validate signed file or a package of files";
	public static final String VALIDATE_PATH_DESCRIPTION = "Obtains a file from POST data upload and creates a list of attached (e.g. xml file attachments) or packed (e.g. zip files) files. The procedure of file extraction is repeated until all nested files are analyzed or until a certain threshold is reached. After that API checks signature validity of every listed file. The resulting report is then sent in response in an array containing each analysed file and its signature status. Fields: dssDetailedReport and dssDiagnosticReport are optional - they will be provided if user sets appropriate option in the application properties.";

	public static final String VALIDATE_PATH_STATUS_200_MESSAGE = "Signature validation was finished and the results are sent in response";
	public static final String VALIDATE_PATH_STATUS_500_MESSAGE = "File error";

	public static final String PARAMS_FILE_NAME = "file";
	public static final String PARAMS_FILE_DESCRITPION = "File to be analyzed";

	public static final String PARAMS_POLICY_NAME = "policy";
	public static final String PARAMS_POLICY_DESCRITPION = "Validation policy file";
	
	public static final String PARAMS_FILE_DATA_TYPE = "java.io.File";
	
	
	public static final String ROOT_PATH_SUMMARY = "API welcome message";
	public static final String ROOT_PATH_DESCRIPTION = "Base API path to test if service is online";
	public static final String ROOT_PATH_STATUS_200_MESSAGE = "Service is up";
	
	
	private SwaggerAnnotations() {
	}
}
