package com.springjpa.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.springjpa.model.Student;
import com.springjpa.repo.StudentRepository;

@org.springframework.stereotype.Service
public class Service {
	
	@Autowired
	private StudentRepository repository;
	
	 	public List<Student> findByLastName(String name) {

	        List<Student> cities = (List<Student>) repository.findByLastName(name);
	        return cities;
	    }
}
