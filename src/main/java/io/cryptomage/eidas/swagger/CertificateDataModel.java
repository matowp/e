package io.cryptomage.eidas.swagger;

import java.util.ArrayList;

import lombok.Getter;

/**
 * A class created for springfox swagger to create a model of a response.
 *        
 * @author Dariusz Napłoszek
 */
@Getter
public class CertificateDataModel {
	private String validation;
	private String subvalidation;
	private String signingDate;
	private String signedBy;
	private String signatureLevel;
	private String signatureFormat;
	private String signatureNumber;
	private String dssDetailedReport;
	private String dssDiagnosticReport;
	private ArrayList<String> errors;
	private ArrayList<String> warnings;
	private ArrayList<String> info;
	private ArrayList<String> timestampList;
	private String subjectData;
	private String issuerData;
	private String signatureAlgorithm;
	private String certificateSerialNumber;
	private String certificateValidityPeriod;
	private String isForSigning;
}
