package com.codingtask;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection; 
import java.sql.DriverManager;

@Configuration
@ComponentScan(basePackages={"com.codingtask"})
public class CodingTaskSetup {
	static final Logger log =	LogManager.getLogger(CodingTaskSetup.class.getName()); 

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public Connection connection() {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			Connection connection = DriverManager.getConnection("jdbc:hsqldb:file:eventdb;ifexists=false", "user", "");
			connection.createStatement().execute("CREATE TABLE IF NOT EXISTS event (id VARCHAR(20), duration INTEGER, type VARCHAR(50), host VARCHAR(50), alert BOOLEAN)");
			return connection;
		} catch (Exception e) {
			log.fatal("JDBCDriver issue");
			throw new BeanCreationException("Connection", "Failed to create a Connection", e);
		}
	}
}
