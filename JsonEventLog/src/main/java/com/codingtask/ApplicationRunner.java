package com.codingtask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.codingtask.dao.EventDao;
import com.codingtask.domain.Event;
import com.codingtask.domain.EventConverter;
import com.codingtask.domain.EventDto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.codingtask.domain.EventDto.State.STARTED;

@SpringBootApplication
public class ApplicationRunner implements CommandLineRunner {
 	
	static final Logger logger =	LogManager.getLogger(ApplicationRunner.class.getName()); 
    private final ObjectMapper objectMapper;
    private final Connection connection;
    private final EventConverter eventConverter;

    private Map<String, EventDto> startedMap = new ConcurrentHashMap<>();
    private Map<String, EventDto> finishedMap = new ConcurrentHashMap<>();

    public static void main(String[] args) { 
        
    	logger.info ("ApplicationRunner starts here");
        SpringApplication.run(ApplicationRunner.class, args);
        
    }

    @Autowired
    public ApplicationRunner(ObjectMapper objectMapper, Connection connection, EventConverter eventConverter) {
        this.objectMapper = objectMapper;
        this.connection = connection;
        this.eventConverter = eventConverter;
    }

    @Override
    public void run(String... args) throws IOException {
        if (args.length != 1 || args[0].isEmpty()) {
            throw new InvalidParameterException("Please provide a single log file filePath argument");
        }

        String filePath = args[0];

        logger.info ("Open file {} for processing", filePath);
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            bufferedReader.lines().forEach(this::groupByState);
            processData(startedMap.keySet());

            logger.info ("Finished processing file {}, closing...", filePath);
        } catch (IOException e) {
        	logger.info ("Failure reading the file, exiting...", e);
             throw e;
        }
    }

    /**
     * Convert even JSON into eventDTO, and saves to start/finish hashmap  based on state 
     *
     * @param json
     */
    private void groupByState(String json) {
        try {
        	logger.info ("Convert JSON to EventDto");
            EventDto eventDTO = Optional.ofNullable(objectMapper.readValue(json, EventDto.class))
                    .orElseThrow(() -> new NullPointerException("Failed to convert json to EventDTO"));

            logger.info ("Create STARTED and FINISHED events map");
            if (eventDTO.getState().equals(STARTED)) {
                startedMap.put(eventDTO.getId(), eventDTO);
            } else {
                finishedMap.put(eventDTO.getId(), eventDTO);
            }
        } catch (IOException e) {
        	logger.info ("Failure processing json {}, skipping...", json);
        }
    }

    /**
     * Takes event ids and finds corresponding start and finish events if found saves resulting event and its duration
     *
     * @param ids
     */
    private void processData(Set<String> ids) {
        try(EventDao eventDao = new EventDao(connection)) {
            for (String id : ids) {
                EventDto startEvent = startedMap.get(id);
                EventDto finishEvent = finishedMap.get(id);
                if (startEvent!= null && finishEvent != null) {
                	logger.info ("Converting eventDTO to event...");
                    Event event = eventConverter.eventDtoToEvent(startEvent, finishEvent);

                    logger.info ("Saving {}", event.toString());
                    eventDao.save(event);
                } else {
                	logger.info ("Log {} ids is missing start or finish event, skipping...", id);
                }
            }
        }
    }
}
