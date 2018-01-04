package io.cryptomage.eidas.swagger;

import java.util.ArrayList;

import lombok.Getter;

/**
 * A class created for springfox swagger to create a model of a response.
 *      
 * @author Dariusz Napłoszek
 */
@Getter
public class FileDataModel {
	String fileName;
	String detachedFiles;
	ArrayList<CertificateDataModel> certificateData;
}
