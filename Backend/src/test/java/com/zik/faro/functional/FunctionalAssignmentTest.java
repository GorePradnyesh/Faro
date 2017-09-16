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
import com.zik.faro.TestUtil;
import com.zik.faro.commons.FaroResponse;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.Item;
import com.zik.faro.data.Location;
import com.zik.faro.data.GeoPosition;
import com.zik.faro.data.Unit;
import com.zik.faro.data.UpdateCollectionRequest;

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
		GeoPosition geoPosition = new GeoPosition(0,0);
    	Event eventCreateData = new Event("Sample functional assignment test", Calendar.getInstance(), Calendar.getInstance(), 
    			false, "Testing functional assignment", null, new
				Location("NY-FunctionalAssignmentTest", "NY-Address", geoPosition), "FunctionalAssignmentUser");
    	
        ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event event = response.getEntity(Event.class);
        FunctionalEventTest.assertEntity(eventCreateData, event);
        eventId = event.getId();
    }
    
    @Test
    public void updateEventAssignmentTodoTest() throws IllegalDataOperation, IOException{
    	ClientResponse response = TestHelper.doGET(endpoint.toString(), "v1/event/"+ eventId+"/details", new MultivaluedMapImpl(), token);
        EventInviteStatusWrapper eventDetails = response.getEntity(EventInviteStatusWrapper.class);
        Event e = eventDetails.getEvent();
    	
        // Add only
        List<Item> toBeAdded = new ArrayList<Item>();
        toBeAdded.add(new Item("food", "Gaurav", 2, Unit.COUNT) );
        toBeAdded.add(new Item("drinks", "Kaivan", 5, Unit.COUNT) );
        UpdateCollectionRequest<Event,Item> requestObj = new UpdateCollectionRequest<>();
        requestObj.setUpdate(e);
        requestObj.setToBeAdded(toBeAdded);
        
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/updateItems", token, requestObj);
        FaroResponse<Event> faroResponse = response.getEntity(new GenericType<FaroResponse<Event>>(){});
        Event updatedEvent = faroResponse.getEntity();
        
        TestUtil.isEqualList(updatedEvent.getAssignment().getItems(), toBeAdded);
        
        // Add, Modify and Delete
        toBeAdded.clear();
        toBeAdded.add(updatedEvent.getAssignment().getItems().get(0));
    	toBeAdded.get(0).setCount(5);
    	toBeAdded.add(new Item("blankets", "Nakul", 10, Unit.COUNT));
    	
    	List<Item> toBeRemoved = new ArrayList<>();
    	toBeRemoved.add(updatedEvent.getAssignment().getItems().get(1));
    	
    	requestObj = new UpdateCollectionRequest<>();
        requestObj.setUpdate(updatedEvent);
        requestObj.setToBeAdded(toBeAdded);
        requestObj.setToBeRemoved(toBeRemoved);
        ClientResponse response1 = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/updateItems", token, requestObj);
        FaroResponse<Event> faroResponse1 = response1.getEntity(new GenericType<FaroResponse<Event>>(){});
        Event updatedEvent1 = faroResponse1.getEntity();
        
        List<Item> expected = new ArrayList<Item>();
        expected.addAll(toBeAdded);
        
        TestUtil.isEqualList(expected, updatedEvent1.getAssignment().getItems());
    	
        toBeRemoved.clear();
        toBeRemoved.add(updatedEvent1.getAssignment().getItems().get(0));
        requestObj = new UpdateCollectionRequest<>();
        requestObj.setUpdate(updatedEvent1);
        requestObj.setToBeRemoved(toBeRemoved);
    	
        ClientResponse response2 = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/assignment/updateItems", token, requestObj);
        FaroResponse<Event> faroResponse2 = response2.getEntity(new GenericType<FaroResponse<Event>>(){});
        Event updatedEvent2 = faroResponse2.getEntity();
        
        expected.clear();
        expected.add(updatedEvent1.getAssignment().getItems().get(1));
    	
        TestUtil.isEqualList(expected, updatedEvent2.getAssignment().getItems());
    }
    
    @Test
    public void updateActivityAssignmentTodoTest() throws Exception{
    	GeoPosition geoPosition = new GeoPosition(0,0);
        Activity activity = new Activity(eventId, "Hiking",
                "Test activity description", new Location("NYC", "NYC's Address", geoPosition),
                Calendar.getInstance(), Calendar.getInstance(),null);
        ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/activity/create", token, activity);
        Activity activityResponse = response.getEntity(Activity.class);
    	
        // Add only
        List<Item> toBeAdded = new ArrayList<Item>();
        toBeAdded.add(new Item("food", "Gaurav", 2, Unit.COUNT) );
        toBeAdded.add(new Item("drinks", "Kaivan", 5, Unit.COUNT) );
        UpdateCollectionRequest<Activity,Item> requestObj = new UpdateCollectionRequest<>();
        requestObj.setUpdate(activityResponse);
        requestObj.setToBeAdded(toBeAdded);
        
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+activityResponse.getEventId()+"/activity/"+activityResponse.getId()+"/assignment/updateItems", token, requestObj);
        FaroResponse<Activity> faroResponse = response.getEntity(new GenericType<FaroResponse<Activity>>(){});
        Activity updatedActivity = faroResponse.getEntity();
        
        TestUtil.isEqualList(updatedActivity.getAssignment().getItems(), toBeAdded);
        
        // Add, Modify and Delete
        toBeAdded.clear();
        toBeAdded.add(updatedActivity.getAssignment().getItems().get(0));
    	toBeAdded.get(0).setCount(5);
    	toBeAdded.add(new Item("blankets", "Nakul", 10, Unit.COUNT));
    	
    	List<Item> toBeRemoved = new ArrayList<>();
    	toBeRemoved.add(updatedActivity.getAssignment().getItems().get(1));
    	
    	requestObj = new UpdateCollectionRequest<>();
        requestObj.setUpdate(updatedActivity);
        requestObj.setToBeAdded(toBeAdded);
        requestObj.setToBeRemoved(toBeRemoved);
        ClientResponse response1 = TestHelper.doPOST(endpoint.toString(), "v1/event/"+activityResponse.getEventId()+"/activity/"+activityResponse.getId()+"/assignment/updateItems", token, requestObj);
        FaroResponse<Activity> faroResponse1 = response1.getEntity(new GenericType<FaroResponse<Activity>>(){});
        Activity updatedActivity1 = faroResponse1.getEntity();
        
        List<Item> expected = new ArrayList<Item>();
        expected.addAll(toBeAdded);
        
        TestUtil.isEqualList(expected, updatedActivity1.getAssignment().getItems());
    	
        toBeRemoved.clear();
        toBeRemoved.add(updatedActivity1.getAssignment().getItems().get(0));
        requestObj = new UpdateCollectionRequest<>();
        requestObj.setUpdate(updatedActivity1);
        requestObj.setToBeRemoved(toBeRemoved);
    	
        ClientResponse response2 = TestHelper.doPOST(endpoint.toString(), "v1/event/"+activityResponse.getEventId()+"/activity/"+activityResponse.getId()+"/assignment/updateItems", token, requestObj);
        FaroResponse<Activity> faroResponse2 = response2.getEntity(new GenericType<FaroResponse<Activity>>(){});
        Activity updatedActivity2 = faroResponse2.getEntity();
        
        expected.clear();
        expected.add(updatedActivity1.getAssignment().getItems().get(1));
    	
        TestUtil.isEqualList(expected, updatedActivity2.getAssignment().getItems());
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

}
