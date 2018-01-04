package io.cryptomage.eidas.controller;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.cryptomage.eidas.service.EidasService;
import io.cryptomage.eidas.swagger.FileDataModel;
import io.cryptomage.eidas.swagger.HelloResponseModel;
import io.cryptomage.eidas.swagger.SwaggerAnnotations;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller for eIDAS signature validation system endpoints.
 * 
 * @author Łukasz Godziejewski and Dariusz Napłoszek
 */
@RestController
@DependsOn({ "eidasService" })
@RequestMapping("/api/v1")
public class EidasController {
	private static final String WELCOME_MSG = "eIDAS signature verification system works!";

	@Autowired
	private EidasService eidasService;

	/**
	 * Validation endpoint which is used to verify signature on sent file.
	 * 
	 * @param file
	 *            File sent by the API consumer.
	 * @param policy
	 *            Validation policy file (not required)
	 * @return validation report.
	 */
	@ApiOperation(value = SwaggerAnnotations.VALIDATE_PATH_SUMMARY, notes = SwaggerAnnotations.VALIDATE_PATH_DESCRIPTION)
	@ApiResponses({ @ApiResponse(code = 200, message = SwaggerAnnotations.VALIDATE_PATH_STATUS_200_MESSAGE, response = FileDataModel.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = SwaggerAnnotations.VALIDATE_PATH_STATUS_500_MESSAGE) })
	@PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> validate(
			@ApiParam(value = SwaggerAnnotations.PARAMS_FILE_DESCRITPION, required = true) MultipartFile file,
			@ApiParam(value = SwaggerAnnotations.PARAMS_POLICY_DESCRITPION) MultipartFile policy) throws IOException {
		String validationResult = eidasService.validate(file, policy);
		return ResponseEntity.ok(validationResult);
	}

	/**
	 * Test endpoint to check if eIDAS signature verification responds.
	 * 
	 * @return eIDAS response
	 */
	@ApiOperation(value = SwaggerAnnotations.ROOT_PATH_SUMMARY, notes = SwaggerAnnotations.ROOT_PATH_DESCRIPTION)
	@ApiResponses({
			@ApiResponse(code = 200, message = SwaggerAnnotations.ROOT_PATH_STATUS_200_MESSAGE, response = HelloResponseModel.class) })
	@GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> hello() {
		JSONObject json = new JSONObject();
		json.put("message", WELCOME_MSG);

		return ResponseEntity.ok(json.toString());
	}
}
