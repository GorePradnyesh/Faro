package com.zik.faro.applogic;

import com.zik.faro.api.bean.Event;
import com.zik.faro.data.EventDo;

public class ConversionUtils {
	public static Event fromDo(EventDo eventDo){
		Event event = new Event();
		event.setAssignment(eventDo.getAssignment());
		event.setControlFlag(eventDo.isControlFlag());
		event.setEndDate(eventDo.getEndDate());
		event.setStartDate(eventDo.getStartDate());
		event.setExpenseGroup(eventDo.getExpenseGroup());
		event.setLocation(eventDo.getLocation());
		event.setEventId(eventDo.getEventId());
		event.setEventName(eventDo.getEventName());
		return event;
	}
	
	public static EventDo toDo(Event event){
		EventDo eventDo = new EventDo();
		eventDo.setAssignment(event.getAssignment());
		eventDo.setControlFlag(event.getControlFlag());
		eventDo.setEndDate(event.getEndDate());
		eventDo.setStartDate(event.getStartDate());
		eventDo.setExpenseGroup(event.getExpenseGroup());
		eventDo.setLocation(event.getLocation());
		eventDo.setEventId(event.getEventId());
		eventDo.setEventName(event.getEventName());
		return eventDo;
	}
}
