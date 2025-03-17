package com.spring.batch.cvstodb.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.spring.batch.cvstodb.listener.JobCompletionNotificationListener;
import com.spring.batch.cvstodb.model.Person;
import com.spring.batch.cvstodb.processor.PersonItemProcessor;

@Configuration
@EnableBatchProcessing
public class Batchconfiguration {
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	public DataSource dataSource;
	
	
	@Bean
	public FlatFileItemReader<Person> reader(){
		FlatFileItemReader<Person> reader=new FlatFileItemReader<Person>();
		reader.setResource(new ClassPathResource("person.csv"));
		reader.setLineMapper(new DefaultLineMapper<Person>() 
		//Ramu,Ramu,ramu@gmail.com,40
				{{
					setLineTokenizer(new DelimitedLineTokenizer() 
							{{
							setNames(new String[] { "firstName","lastName","email","age" });
							}});
							setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>()
									{{
										setTargetType(Person.class);
										//Person.setFirstname(arra["firstName"]);
										//Person.setLastName(arra["lastName"])
										//Person.setEmail(arra["email"])
										//Person.setAge(arra["age"])

										
									}});
				}}
							);
			
	return reader;
	}
	
	@Bean
	public PersonItemProcessor processor() {
		return new PersonItemProcessor();
	}
	
	
	@Bean
public JdbcBatchItemWriter<Person> writer() {
    JdbcBatchItemWriter<Person> write = new JdbcBatchItemWriter<>();
    write.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
    
    // INSERT IGNORE to prevent duplicate key errors
    // write.setSql("INSERT IGNORE INTO PERSON(FIRST_NAME,LAST_NAME,EMAIL,AGE) VALUES(:firstName,:lastName,:email,:age)");
    // write.setSql("INSERT INTO PERSON (firstName, lastName, EMAIL, AGE) " +
    //          "VALUES (:firstName, :lastName, :email, :age) " +
    //          "ON DUPLICATE KEY UPDATE " +
    //          "firstName = VALUES(firstName), " +
    //          "lastName = VALUES(lastName), " +
    //          "AGE = VALUES(AGE)");
	write.setSql("INSERT INTO PERSON (first_name, last_name, email, age) " +
             "VALUES (:firstName, :lastName, :email, :age) " +
             "ON DUPLICATE KEY UPDATE " +
             "first_name = VALUES(first_name), " +
             "last_name = VALUES(last_name), " +
             "age = VALUES(age)");


    write.setDataSource(dataSource);
    return write;
}

	
	
	@Bean
	public Step step1() {
		return stepBuilderFactory.get("newstep")
				.<Person,Person> chunk(100)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}
	
	
	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener) {
		return jobBuilderFactory.get("importUserJob")
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(step1())
				.end()
				.build();
		
	}
	
	
}
