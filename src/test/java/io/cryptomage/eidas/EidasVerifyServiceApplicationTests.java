package io.cryptomage.eidas;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class EidasVerifyServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

	
	public static JSONArray loadSignatures() throws FileNotFoundException {
		File source = new File("src/test/resources/testFiles.json");
		Scanner scanner = new Scanner(source);
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		JSONArray jsonArray = new JSONArray(content);
		return jsonArray;
	}

	@Test
	public void isServerRunning() throws Exception {
		this.mockMvc.perform(get("/api/v1/").accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        		.andExpect(status().isOk())
        		.andExpect(content().contentType("application/json;charset=UTF-8"));
	}
	
	@Test
	public void getFiles() throws FileNotFoundException {
		JSONArray jsonArray = loadSignatures();
		assertNotNull(jsonArray);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = jsonArray.getJSONObject(i);
			assertNotNull(obj);
			String filePath = obj.getString("filePath");
			assertNotNull(filePath);
			File file = new File(filePath);
			assertEquals(file.exists(), true);
			String signedFile = obj.getString("signedFile");
			assertNotNull(signedFile);
			String result = obj.getString("result");
			assertNotNull(result);
			int fileCount = obj.getInt("fileCount");
			assertNotNull(fileCount);
		}
	}

	@Test
	public void validateSignaturesTest() throws Exception {
		JSONArray jsonArray = loadSignatures();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = jsonArray.getJSONObject(i);
			String filePath = obj.getString("filePath");
			File file = new File(filePath);
			FileInputStream input = new FileInputStream(file);
			MockMultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
					IOUtils.toByteArray(input));
			MvcResult result = mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/v1/validate")
					.file(multipartFile))
            		.andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
					.andReturn();
			
			checkResult(result.getResponse().getContentAsString(), obj);
			/**/
		}
	}

	private void checkResult(String result, JSONObject sampleObj) {
		assertNotNull(result);
		JSONArray retArray = new JSONArray(result);
		assertEquals(sampleObj.getInt("fileCount"), retArray.length());
		for (int j = 0; j < retArray.length(); j++) {
			JSONObject json = retArray.getJSONObject(j);
			String resultFileName = json.getString("fileName");
			String sampleFileName = sampleObj.getString("signedFile");
			if (resultFileName.equals(sampleFileName)) {
				JSONObject cert = (JSONObject) json.getJSONArray("certificateData").get(0);
				assertEquals(sampleObj.getString("result"), cert.getString("validation"));
			}
		}
		
	}
}
