package com.codingtask.dao;

import com.codingtask.constants.CodingTaskConstants;
import com.codingtask.domain.Event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class EventDao implements AutoCloseable {
	static final Logger log =	LogManager.getLogger(EventDao.class.getName()); 

    private Connection connection;
    private final static String sql = "INSERT INTO event (id, duration, type, host, alert)  VALUES (?, ?, ?, ?, ?)";

    public EventDao(Connection connection) { 
        this.connection = connection;
    }

    public Boolean save(Event event) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, event.getId());
            statement.setLong(2, event.getDuration());
            statement.setString(3, event.getType());
            statement.setString(4, event.getHost());
            statement.setBoolean(5, event.isAlert());
            return statement.executeUpdate() > CodingTaskConstants.CONSTANT_ZERO;
        } catch (Exception e) {
            log.fatal("Failure saving event, skipping",  e);
            return false;
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
        	log.fatal("Failure closing database connection",  e);
        }
    }
}
