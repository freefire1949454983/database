package com.spring.batch.cvstodb.listener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.spring.batch.cvstodb.model.Person;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate=jdbcTemplate;
	}
	
	@Override
	public void afterJob(JobExecution jobExecution) {
		if(jobExecution.getStatus()==org.springframework.batch.core.BatchStatus.COMPLETED)
		{
			
			List<Person> results = jdbcTemplate.query(
				"SELECT first_name, last_name, email, age FROM PERSON",
				new RowMapper<Person>() {
					@Override
					public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
						return new Person(
							rs.getString("first_name"), 
							rs.getString("last_name"),
							rs.getString("email"), 
							rs.getInt("age")
						);
					}
				}
			);
			
			// for (Person person : results) {
			// 	System.out.println("Found <" + person + "> in the database.");
			// }
		}
		
	}
}
