package com.zik.faro.functional;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

//    public static void createAndReadActivityTest() throws Exception{
//        Assignment assignment = new Assignment("HikingToDo", ActionStatus.INCOMPLETE);
//        assignment.addItem(new Item("food", "Gaurav", 2, Unit.COUNT));
//        assignment.addItem(new Item("drinks", "Paddy", 5, Unit.COUNT));
//        // Create sample activity without id
//        Activity activity = new Activity(eventId, "Hiking",
//                "Test activity description", new Location("NYC"),
//                Calendar.getInstance(), Calendar.getInstance(),assignment);
//        ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/activity/create", token, activity);
//        Activity activityResponse = response.getEntity(Activity.class);
//        assertEntity(activity, activityResponse);
//        // Read recently created activity
//        ClientResponse readResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/activity/"+activityResponse.getId(), new MultivaluedMapImpl(), token);
//        activityResponse = readResponse.getEntity(Activity.class);
//        assertEntity(activity, activityResponse);
//    }
    
    public void updateEventAssignmentTodo() throws IllegalDataOperation, IOException{
    	ClientResponse response1 = TestHelper.doGET(endpoint.toString(), "v1/event/"+ eventId+"/details", new MultivaluedMapImpl(), token);
        Event eventDetails = response1.getEntity(Event.class);
        
    	// Add item-todo to assignment
    	List<Item> items = new ArrayList<Item>();
    	items.add(new Item("food", "Gaurav", 2, Unit.COUNT) );
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/"+eventDetails.getAssignment().getId()+"/updateItems", token, items);
    	List<Item> updatedItems = response.getEntity(new GenericType<List<Item>>(){});
    	Assert.assertEquals(items.size(), updatedItems.size());
    	items = updatedItems;
    	
    	// Verify
    	response1 = TestHelper.doGET(endpoint.toString(), "v1/event/"+ eventId+"/details", new MultivaluedMapImpl(), token);
        eventDetails = response1.getEntity(Event.class);
        assertTodo(items, eventDetails.getAssignment().getItems());
    	// update above added item-todo
        
        items.get(0).setCount(5);
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/"+eventDetails.getAssignment().getId()+"/updateItems", token, items);
    	updatedItems = response.getEntity(new GenericType<List<Item>>(){});
    	assertTodo(items, updatedItems);
    	items = updatedItems;
    	
    	// Verify
    	response1 = TestHelper.doGET(endpoint.toString(), "v1/event/"+ eventId+"/details", new MultivaluedMapImpl(), token);
        eventDetails = response1.getEntity(Event.class);
        assertTodo(items, eventDetails.getAssignment().getItems());
        
    	// add and update item-todo
    	items.get(0).setAssigneeId("Kaivan");
    	items.get(0).setCount(10);
    	items.get(0).setStatus(ActionStatus.COMPLETE);
    	
    	items.add(new Item("drinks", "Kunal", 3, Unit.COUNT));
    	response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/"+eventDetails.getAssignment().getId()+"/updateItems", token, items);
    	updatedItems = response.getEntity(new GenericType<List<Item>>(){});
    	Assert.assertEquals(items.size(), updatedItems.size());
    	items=updatedItems;
    	
    	// Verify
    	response1 = TestHelper.doGET(endpoint.toString(), "v1/event/"+ eventId+"/details", new MultivaluedMapImpl(), token);
        eventDetails = response1.getEntity(Event.class);
        assertTodo(items, eventDetails.getAssignment().getItems());
    }
    
    public void updateActivityAssignmentTodo() throws IllegalDataOperation, IOException{
    	
    	// Create activity
		Activity activity = new Activity(eventId, "Hiking",
				"Test activity description", new Location("NYC"),
				Calendar.getInstance(), Calendar.getInstance(), null);
		ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/activity/create", token, activity);
		Activity activityResponse = response.getEntity(Activity.class);
		FunctionalActivityTest.assertCreatedEntity(activity, activityResponse);
		// Read recently created activity
		ClientResponse readResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/activity/"+activityResponse.getId(), new MultivaluedMapImpl(), token);
		activityResponse = readResponse.getEntity(Activity.class);
		FunctionalActivityTest.assertCreatedEntity(activity, activityResponse);
		
    	// Add item-todo to assignment
    	List<Item> items = new ArrayList<Item>();
    	MultivaluedMap<String,String> map = new MultivaluedMapImpl();
    	map.add("activityId", activityResponse.getId());
    	
    	items.add(new Item("food", "Gaurav", 2, Unit.COUNT) );
    	response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/"+activityResponse.getAssignment().getId()+
    			"/updateItems", token, items, map);
    	List<Item> updatedItems = response.getEntity(new GenericType<List<Item>>(){});
    	Assert.assertEquals(items.size(), updatedItems.size());
    	items = updatedItems;
    	
    	// Verify
    	response = TestHelper.doGET(endpoint.toString(), "v1/event/"+ eventId+"/activity/"+activityResponse.getId(), new MultivaluedMapImpl(), token);
        activityResponse = response.getEntity(Activity.class);
        assertTodo(items, activityResponse.getAssignment().getItems());
    	// update above added item-todo
        
        items.get(0).setCount(5);
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/"+activityResponse.getAssignment().getId()+
    			"/updateItems", token, items, map);
        updatedItems = response.getEntity(new GenericType<List<Item>>(){});
        assertTodo(items, updatedItems);
        items=updatedItems;
    	
    	// Verify
    	response = TestHelper.doGET(endpoint.toString(), "v1/event/"+ eventId+"/activity/"+activityResponse.getId(), new MultivaluedMapImpl(), token);
        activityResponse = response.getEntity(Activity.class);
        assertTodo(items, activityResponse.getAssignment().getItems());
        
    	// add and update item-todo
    	items.get(0).setAssigneeId("Kaivan");
    	items.get(0).setCount(10);
    	items.get(0).setStatus(ActionStatus.COMPLETE);
    	
    	items.add(new Item("drinks", "Kunal", 3, Unit.COUNT));
    	response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/"+activityResponse.getAssignment().getId()+
    			"/updateItems", token, items, map);
    	updatedItems = response.getEntity(new GenericType<List<Item>>(){});
    	Assert.assertEquals(items.size(), updatedItems.size());
    	items = updatedItems;
    	
    	// Verify
    	response = TestHelper.doGET(endpoint.toString(), "v1/event/"+ eventId+"/activity/"+activityResponse.getId(), new MultivaluedMapImpl(), token);
        activityResponse = response.getEntity(Activity.class);
        assertTodo(items, activityResponse.getAssignment().getItems());
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
    	updateEventAssignmentTodo();
    	updateActivityAssignmentTodo();
    }
}
