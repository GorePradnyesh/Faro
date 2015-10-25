package com.zik.faro.applogic;

import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.data.Poll;
import com.zik.faro.data.ActivityDo;
import com.zik.faro.data.EventDo;
import com.zik.faro.data.PollDo;

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
	
	public static ActivityDo toDo(Activity activity){
		ActivityDo activityDo = new ActivityDo();
		activityDo.setAssignment(activity.getAssignment());
		activityDo.setDate(activity.getDate());
		activityDo.setDescription(activity.getDescription());
		activityDo.setLocation(activity.getLocation());
		activityDo.setId(activity.getId());
		activityDo.setEventId(activity.getEventId());
		activityDo.setName(activity.getName());
		return activityDo;
	}
	
	public static Activity fromDo(ActivityDo activityDo){
		Activity activity = new Activity();
		activity.setAssignment(activityDo.getAssignment());
		activity.setDate(activityDo.getDate());
		activity.setDescription(activityDo.getDescription());
		activity.setLocation(activityDo.getLocation());
		activity.setId(activityDo.getId());
		activity.setEventId(activityDo.getEventId());
		activity.setName(activityDo.getName());
		return activity;
	}
	
	public static Poll fromDo(PollDo pollDo){
		Poll poll = new Poll();
		poll.setCreatorId(pollDo.getCreatorId());
		poll.setDeadline(pollDo.getDeadline());
		poll.setEventId(pollDo.getEventId());
		poll.setDescription(pollDo.getDescription());
		poll.setId(pollDo.getId());
		poll.setOwner(pollDo.getOwner());
		poll.setStatus(pollDo.getStatus());
		poll.setWinnerId(pollDo.getWinnerId());
		poll.setPollOptions(pollDo.getPollOptions());
		return poll;
	}
	
	public static PollDo toDo(Poll poll){
		PollDo pollDo = new PollDo();
		pollDo.setCreatorId(poll.getCreatorId());
		pollDo.setDeadline(poll.getDeadline());
		pollDo.setEventId(poll.getEventId());
		pollDo.setDescription(poll.getDescription());
		pollDo.setId(poll.getId());
		pollDo.setOwner(poll.getOwner());
		pollDo.setStatus(poll.getStatus());
		pollDo.setWinnerId(poll.getWinnerId());
		pollDo.setPollOptions(poll.getPollOptions());
		return pollDo;
	}
}
