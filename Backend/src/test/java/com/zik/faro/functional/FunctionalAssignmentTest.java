package com.zik.faro.functional;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.zik.faro.TestHelper;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.Item;
import com.zik.faro.data.Location;
import com.zik.faro.data.Unit;

public class FunctionalAssignmentTest {
	private static URL endpoint;
    private static String token = null;
    private static String eventId = null;

    @BeforeClass
    public static void init() throws Exception {
        endpoint = TestHelper.getExternalTargetEndpoint();
        token = TestHelper.createUserAndGetToken();
        createEvent();
    }

    // Create an event for all activity tests
    private static void createEvent() throws IOException{
    	Event eventCreateData = new Event("Sample functional assignment test", Calendar.getInstance(), Calendar.getInstance(), 
    			false, "Testing functional assignment", null, new Location("NY-FunctionalAssignmentTest"), "FunctionalAssignmentUser");
    	
        ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event event = response.getEntity(Event.class);
        FunctionalEventTest.assertEntity(eventCreateData, event);
        eventId = event.getEventId();
    }
    
    public void updateEventAndActivityAssignmentTodo() throws IllegalDataOperation, IOException{
    	ClientResponse response1 = TestHelper.doGET(endpoint.toString(), "v1/event/"+ eventId+"/details", new MultivaluedMapImpl(), token);
        Event eventDetails = response1.getEntity(Event.class);
        
    	// Add event level assignment
        Map<String,List<Item>> map = new HashMap<String,List<Item>>();
    	List<Item> items = new ArrayList<Item>();
    	items.add(new Item("food", "Gaurav", 2, Unit.COUNT) );
    	map.put(eventId, items);
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/"+eventDetails.getAssignment().getId()+"/updateItems", token, map);
    	Map<String,List<Item>> updatedItems = response.getEntity(new GenericType<Map<String,List<Item>>>(){});
    	Assert.assertEquals(map.size(), updatedItems.size());
    	map = updatedItems;
    	
    	// Verify Event Level
    	response1 = TestHelper.doGET(endpoint.toString(), "v1/event/"+ eventId+"/details", new MultivaluedMapImpl(), token);
        eventDetails = response1.getEntity(Event.class);
        assertTodo(map.get(eventId), eventDetails.getAssignment().getItems());
    	
        // update above added item-todo for event level
        items.get(0).setCount(5);
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/"+eventDetails.getAssignment().getId()+"/updateItems", token, map);
        updatedItems = response.getEntity(new GenericType<Map<String,List<Item>>>(){});
    	assertTodo(map.get(eventId), updatedItems.get(eventId));
    	map = updatedItems;
    	
    	// Create activity
		Activity activity = new Activity(eventId, "Hiking",
				"Test activity description", new Location("NYC"),
				Calendar.getInstance(), Calendar.getInstance(), null);
		response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/activity/create", token, activity);
		Activity activityResponse = response.getEntity(Activity.class);
		FunctionalActivityTest.assertCreatedEntity(activity, activityResponse);
		// Read recently created activity
		ClientResponse readResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/activity/"+activityResponse.getId(), new MultivaluedMapImpl(), token);
		activityResponse = readResponse.getEntity(Activity.class);
		FunctionalActivityTest.assertCreatedEntity(activity, activityResponse);
		
		// Add item-todo to activity assignment
    	List<Item> activityItems = new ArrayList<Item>();
    	
    	activityItems.add(new Item("food", "Gaurav", 2, Unit.COUNT));
    	map.put(activityResponse.getId(), activityItems);
    	response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/"+eventDetails.getAssignment().getId()+"/updateItems", token, map);
    	updatedItems = response.getEntity(new GenericType<Map<String,List<Item>>>(){});
    	Assert.assertEquals(map.size(), updatedItems.size());
    	map = updatedItems;

    	// Verify Activity Level
    	response = TestHelper.doGET(endpoint.toString(), "v1/event/"+ eventId+"/activity/"+activityResponse.getId(), new MultivaluedMapImpl(), token);
        activityResponse = response.getEntity(Activity.class);
        assertTodo(map.get(activityResponse.getId()), activityResponse.getAssignment().getItems());
        
    	// add and update item-todo for activity level
    	activityItems.get(0).setAssigneeId("Kaivan");
    	activityItems.get(0).setCount(10);
    	activityItems.get(0).setStatus(ActionStatus.COMPLETE);
    	
    	// add and update item-todo for event level
    	items.get(0).setCount(10);

    	activityItems.add(new Item("drinks", "Kunal", 3, Unit.COUNT));
    	response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/"+eventDetails.getAssignment().getId()+"/updateItems", token, map);
        updatedItems = response.getEntity(new GenericType<Map<String,List<Item>>>(){});
        assertTodo(map.get(eventId), updatedItems.get(eventId));
    	assertTodo(map.get(activityResponse.getId()), updatedItems.get(activityResponse.getId()));

    }
    
    private void assertTodo(List<Item> expected, List<Item> actual){
    	int assertCount = 0;
    	for(Item item: expected){
    		for(Item item_actual: actual){
    			if(item.getId().equals(item_actual.getId())){
    				Assert.assertEquals(item.getAssigneeId(),item_actual.getAssigneeId());
    				Assert.assertEquals(item.getName(),item_actual.getName());
    				Assert.assertEquals(item.getCount(),item_actual.getCount());
    				Assert.assertEquals(item.getStatus(),item_actual.getStatus());
    				assertCount++;
    			}
    		}
    	}
    	Assert.assertEquals(expected.size(), assertCount);
    }
    
    @Test
    public void allTest() throws IllegalDataOperation, IOException{
    	updateEventAndActivityAssignmentTodo();
    }
}
