package com.springjpa.services;

import java.util.List;

import com.springjpa.model.Student;

public interface StudentService {
	public List<Student> findByLastName(String lastName);
}
