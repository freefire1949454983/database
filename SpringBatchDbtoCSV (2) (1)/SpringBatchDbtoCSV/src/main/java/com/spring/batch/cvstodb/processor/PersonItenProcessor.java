package com.spring.batch.cvstodb.processor;

import org.springframework.batch.item.ItemProcessor;

import com.spring.batch.cvstodb.model.Person;

public class PersonItenProcessor implements ItemProcessor<Person, Person>{
	
	//ItemProcessor two inputs
	//1st input ==> Output of the Source System
	//2nd Input ==> Input to writer (Destination format)

	@Override
	public Person process(Person person) throws Exception {
		return person;
	}
}
