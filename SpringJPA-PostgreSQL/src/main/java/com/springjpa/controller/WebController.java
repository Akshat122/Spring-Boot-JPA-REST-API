package com.springjpa.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.springjpa.model.Student;
import com.springjpa.repo.StudentRepository;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

@RestController
public class WebController {
	@Autowired
	StudentRepository repository;
	
	
	
	@PostMapping("/add")
	public String process(@RequestBody String student_post ) throws IllegalAccessException, IOException{
		// save a single Student

		//Validating the JSON
		 
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		 
		File file = new File(classLoader.getResource("AddStudent.json").getFile());
		String str = FileUtils.readFileToString(file, "UTF-8");
		boolean dataValid = false;
		try  {
			  JSONObject rawSchema = new JSONObject(new JSONTokener(str));
			  Schema schema = SchemaLoader.load(rawSchema);
			  schema.validate(new JSONObject(student_post)); // throws a ValidationException if this object is invalid
			  dataValid = true ;
			  System.out.println("Data is Valid");
			}
		catch(Exception e){
			System.out.print("Error: "+e.getMessage());
			return "{\n \"Error\" : \"Not a valid JSON format\"\n}";
		}
		
		
		if(dataValid) {
			Gson gson = new Gson(); // Or use new GsonBuilder().create();
			Student target2 = gson.fromJson(student_post, Student.class); 
		
		Student new_student = new Student(target2.getFirstName(),target2.getLastName());
		
		//Save the student
		repository.save(new_student);
		
		//Convert response to JSON
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(new_student);
		return json;
		}
		else
		{
			return "";
		}
		
	}
	
	
	@RequestMapping(value = "/showall")
	public Iterable<Student> findAll() throws JsonProcessingException, JSONException{
	
		return repository.findAll();
	}
	
	@RequestMapping("/findbyid")
	public String findById(@RequestParam("id") long id) throws JsonProcessingException{
		Student result ;
		result = repository.findById(id).get();
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(result);
		return json;
	}
	
	@RequestMapping("/checkid")
	public String checkId(@RequestParam("id") long id) throws JsonProcessingException{
		Optional<Student> result ;
		result = repository.findById(id);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(result);
		return json;
	}
	
	
	@PatchMapping("/update/{id}")
	public String update(@RequestBody Student student_post , @PathVariable("id") Long id) throws JsonProcessingException{
	
		
		// get the student details 
		Student result = repository.findById(id).get();
		
		if(student_post.getFirstName()!=null)
		{
			result.setFirstName(student_post.getFirstName());
		}
		if(student_post.getLastName()!=null)
		{
			result.setLastName(student_post.getLastName());
		}
		
		// Save the new Details of the user
		repository.save(result);
		
		//Convert the response to JSON 
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(result);
		return json;
		
	}
	
	@DeleteMapping("/delete/{id}")
	public void deleteStudent(@PathVariable long id) {
		repository.deleteById(id);
	}
	//GraphQL 
	private GraphQL graphql;
	
	@PostConstruct
	public void loadSchema() throws IOException {
		File schemaFile = schema.getFile();
		TypeDefinitionRegistry registory = new SchemaParser().parse(schemaFile);
		RuntimeWiring wiring = buildRuntimeWiring();
		GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(registory, wiring);
		graphql= GraphQL.newGraphQL(graphQLSchema).build();
	}
	
	private RuntimeWiring buildRuntimeWiring() {
		DataFetcher<List<Student>> fetcher1=data->{
			return (List<Student>) repository.findAll();
		};
		
		
		
		return RuntimeWiring.newRuntimeWiring().type("Query",typeWiring->
			typeWiring.dataFetcher("Students", fetcher1)).build();
		
	}

	@Value("classpath:student.graphqls")
	private Resource schema;
	

	@PostMapping("/testing")
	public ResponseEntity<Object> getAllStud(@RequestBody String query){
		ExecutionResult executionResult =graphql.execute(query);
		return new ResponseEntity<Object>(executionResult,HttpStatus.OK);
		
	}
	
	
	
}

