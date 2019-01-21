package com.springjpa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springjpa.model.Student;

import org.apache.commons.io.FileUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class TestCases extends SpringJpaPostgreSqlApplicationTests{
	
	
	
	
	
	@Autowired
	TestRestTemplate TestrestTemplate;
		@Test
		public void testingGetAll() throws JSONException,  org.json.JSONException {
			//Get all users
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.getForEntity(
			        "http://127.0.0.1:8080/showall",
			        String.class);
			JSONArray jsonObj = new JSONArray(response.getBody());
			//Checking the 1st user of the database
			assertEquals(jsonObj.getJSONObject(0).toString(),"{\"firstName\":\"vandit32444432323\",\"lastName\":\"agarwal\",\"id\":47}");
			
			
			

			
		}
	
		@Test
		public void testingAdd() throws  org.json.JSONException, IOException, IllegalAccessException {
			
			//Add new User
			RestTemplate restTemplate = new RestTemplate();
			
					//Json String
					String jsonData = "{\n" + 
							"	\"firstName\": \"Test-Akshat\",\n" + 
							"	\"lastName\": \"Khanna\"\n" + 
							"}";
					//Validating the JSON
					File file = ResourceUtils.getFile(this.getClass().getResource("AddStudent.json"));
					String str = FileUtils.readFileToString(file, "UTF-8");
					try  {
						  JSONObject rawSchema = new JSONObject(new JSONTokener(str));
						  Schema schema = SchemaLoader.load(rawSchema);
						  schema.validate(new JSONObject(jsonData)); // throws a ValidationException if this object is invalid
						}
					catch(Exception e){
						System.out.print("Error: "+e.getMessage());
						 throw new IllegalAccessException("Not a valid JSON Format"); 
					}
					//Headers for Json Data
					HttpHeaders headers = new HttpHeaders();
		            headers.setContentType(MediaType.APPLICATION_JSON);
		            headers.set("Authenticate", "TOKEN");

				    //Create a HttpEntity of the JSON data 
		            HttpEntity<String> entity = new HttpEntity<String>(jsonData, headers);
	
			
			//Sending the Post Request to the URl
			ResponseEntity<String> response = restTemplate.postForEntity("http://127.0.0.1:8080/add",entity,String.class);
			JSONObject jsonObj =  new JSONObject(response.getBody());
			int id = jsonObj.getInt("id");
			
			//get the user
			response = restTemplate.getForEntity("http://127.0.0.1:8080/findbyid?id="+id,String.class);
			JSONObject jsonObj2 =  new JSONObject(response.getBody());
			assertEquals(jsonObj2.getString("firstName"),"Test-Akshat");
		}
		
		
		
		@Test
		public void testingUpdate() throws org.json.JSONException, IllegalAccessException, IOException {
			
			RestTemplate restTemplate = new RestTemplate();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setConnectTimeout(500);
			requestFactory.setReadTimeout(500);
			restTemplate.setRequestFactory(requestFactory);
			
			
			//Add new User
			String jsonData = "{\n" + 
					"	\"firstName\": \"Test-Update-Akshat\",\n" + 
					"	\"lastName\": \"Khanna\"\n" + 
					"}";
			//Validating the JSON
			File file = ResourceUtils.getFile(this.getClass().getResource("AddStudent.json"));
			String str = FileUtils.readFileToString(file, "UTF-8");
			try  {
				  JSONObject rawSchema = new JSONObject(new JSONTokener(str));
				  Schema schema = SchemaLoader.load(rawSchema);
				  schema.validate(new JSONObject(jsonData)); // throws a ValidationException if this object is invalid
				}
			catch(Exception e){
				System.out.print("Error: "+e.getMessage());
				 throw new IllegalAccessException("Not a valid JSON Format"); 
			}
			//Headers for Json Data
			HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authenticate", "TOKEN");

		    //Create a HttpEntity of the JSON data 
            HttpEntity<String> entity = new HttpEntity<String>(jsonData, headers);
			//Sending Post request
			ResponseEntity<String> response = TestrestTemplate.postForEntity("http://127.0.0.1:8080/add",entity,String.class);
			JSONObject jsonObj =  new JSONObject(response.getBody());
			int id = jsonObj.getInt("id");

			
			//Update the user
	        String data = "{\n" + 
	        		"	\"firstName\": \"vanditbbbb\"\n" + 
	        		"}";
	        HttpHeaders headers2 = new HttpHeaders();
	        headers2.setContentType( MediaType.APPLICATION_JSON );
	        HttpEntity request= new HttpEntity( data, headers2 );
	        String responseEntity = TestrestTemplate.patchForObject("http://127.0.0.1:8080/update/"+id, request, String.class);
//	        System.out.println(responseEntity);
	 
//			
//			//Get the user
			response = restTemplate.getForEntity("http://127.0.0.1:8080/findbyid?id="+id,String.class);
			JSONObject jsonObj2 =  new JSONObject(response.getBody());
			assertEquals(jsonObj2.getString("firstName"),"vanditbbbb");
//			
			
		}
		
		@Test
		public void testingDelete() throws JSONException, org.json.JSONException {
			
			//Add new user
			RestTemplate restTemplate = new RestTemplate();
			Student student =  new Student("Del-Test-Akshat","Khanna");
			ResponseEntity<String> response = restTemplate.postForEntity("http://127.0.0.1:8080/add",student,String.class);
			JSONObject jsonObj =  new JSONObject(response.getBody());
			int id = jsonObj.getInt("id");
//			System.out.println("Del-New-UserID-> "+response);
			
			//Delete the user
			restTemplate.delete("http://127.0.0.1:8080/delete/"+id);

//			//Check the Deleted user
			response = restTemplate.getForEntity("http://127.0.0.1:8080/checkid?id="+id,String.class);
//			System.out.println(response);
			JSONObject jsonObj2 =  new JSONObject(response.getBody());
			assertEquals(jsonObj2.getBoolean("present"),false);
		}
		
		
}
