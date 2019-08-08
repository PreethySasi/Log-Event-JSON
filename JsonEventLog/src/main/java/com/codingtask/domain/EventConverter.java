package com.codingtask.domain;

import org.springframework.stereotype.Component;

import com.codingtask.constants.CodingTaskConstants;

@Component
public class EventConverter {
 
    public Event eventDtoToEvent(EventDto startEvent, EventDto finishEvent) {
         Long duration =  finishEvent.getTimestamp() - startEvent.getTimestamp();
        boolean isAlert = duration > CodingTaskConstants.ALERT_VALUE;
        return new Event(startEvent.getId(), duration, startEvent.getType(), startEvent.getHost(), isAlert); 
    }
}
