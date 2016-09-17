package com.zik.faro.functional;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.zik.faro.TestHelper;
import com.zik.faro.data.AddFriendRequest;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.Location;
import com.zik.faro.data.InviteeList.Invitees;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.user.EventInviteStatus;

public class FunctionalEventTest {
    private static URL endpoint;
    private static String token = null;
    // User with whose creds we will be testing all the apis
    private static String callerEmail = UUID.randomUUID().toString() + "@gmail.com";
    
    @BeforeClass
    public static void init() throws Exception {
        endpoint = TestHelper.getExternalTargetEndpoint();
        token = TestHelper.createUserAndGetToken(callerEmail);
        //token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0Njg1ODkwNzMsInVzZXJuYW1lIjoiNzY0ZmZlYjAtZGViMi00MTdhLTkyNzYtZWRkZmRiZGFkNzUwQGdtYWlsLmNvbSIsImVtYWlsIjoiNzY0ZmZlYjAtZGViMi00MTdhLTkyNzYtZWRkZmRiZGFkNzUwQGdtYWlsLmNvbSIsImlzcyI6ImZhcm8iLCJpYXQiOjE0NjM0MDUwNzN9.Ktv4YbXV8BrJsYSHdBikpEwINNF-q8iTLUBhzr9cVZA";
         }
    
    
    // **** Tests ****
    
    // 1. createEvent
    public void createEventTest() throws Exception {
    	for(int i = 0 ; i < 10; i++){
    		Event eventCreateData = new Event("MySampleEvent", Calendar.getInstance(),
                    Calendar.getInstance(), false,"Description",null, new Location("Random Location"), "Mafia god");
    		new Event("MySampleEvent", Calendar.getInstance(),
                    Calendar.getInstance(), false, "Description", null, new Location("Random Location"), null);
        	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
            Event event = response.getEntity(Event.class);
            assertEntity(eventCreateData, event);
    	}
    }
    
    // 2. getEventDetails
    public void getEventDetails() throws Exception{
    	Event eventCreateData = new Event("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), false, "Description", null, new Location("NYC"),"Mafia god");
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event ev = response.getEntity(Event.class);
        ClientResponse response1 = TestHelper.doGET(endpoint.toString(), "v1/event/"+ ev.getEventId()+"/details", new MultivaluedMapImpl(), token);
        Event eventDetails = response1.getEntity(Event.class);
        assertEntity(eventCreateData, eventDetails);
    }
    
    // 3. getEventInvitees
    public void getEventInvitees() throws Exception{
    	// Create event
    	Event eventCreateData = new Event("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), false, "Description", null, new Location("NYC"), "Mafia god");
    	ClientResponse createEventResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event ev = createEventResponse.getEntity(Event.class);
        // Verify indeed created by doing GET
        ClientResponse eventDetailsResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ ev.getEventId()+"/details", new MultivaluedMapImpl(), token);
        Event eventDetails = eventDetailsResponse.getEntity(Event.class);
        assertEntity(eventCreateData, eventDetails);
        String[] friendIds = new String[4];
        for(int i = 0 ; i < friendIds.length ; i++){
        	friendIds[i] = UUID.randomUUID().toString() + "@gmail.com";
        }
        
        // Add multiple friends to event(First create, then add to event)
        // Creating friends
        String faroUser1 = TestHelper.createUser(friendIds[0], "Kaivan", "Vijay", "Sanghvi");
        String faroUser2 = TestHelper.createUser(friendIds[1], "Pradnyesh", "", "Gore");
        String faroUser3 = TestHelper.createUser(friendIds[2], "Gaurav", "", "Rangnathan");
        String faroUser4 = TestHelper.createUser(friendIds[3], "Naku", "Harshad", "Shah");
        
        // Add faro users to event
        List<String> friends = new ArrayList<String>();
        friends.add(faroUser4);friends.add(faroUser3);friends.add(faroUser2);friends.add(faroUser1);
        friends.add("not_a_user@farotest.com");
        AddFriendRequest addFriendRequest = new AddFriendRequest();
        addFriendRequest.setFriendIds(friends);
        ClientResponse addFriendsResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/"+ev.getEventId()+"/add", token, addFriendRequest);
        // Verify via status code since API only returns HTTP_OK
        Assert.assertEquals(addFriendsResponse.getStatus(), 200);
        Assert.assertEquals(addFriendsResponse.getEntity(String.class), "OK");
        
        ClientResponse eventInviteesResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ev.getEventId()+"/invitees", new MultivaluedMapImpl(), token);
        eventInviteesResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ev.getEventId()+"/invitees", new MultivaluedMapImpl(), token);
        eventInviteesResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ev.getEventId()+"/invitees", new MultivaluedMapImpl(), token);
        InviteeList eventInvitees = eventInviteesResponse.getEntity(InviteeList.class);
        Map<String,Invitees> invitees = eventInvitees.getUserStatusMap();
        // Friends + event creator are part of event invitees
        Assert.assertEquals(friends.size()+1, invitees.size());
        
        // Verify returned object for getInvitees API
        // 1. Existing Faro User
        Assert.assertEquals("Kaivan", invitees.get(faroUser1).getFirstName());
        Assert.assertEquals("Vijay", invitees.get(faroUser1).getLastName());
        Assert.assertEquals(EventInviteStatus.INVITED, invitees.get(faroUser1).getInviteStatus());
        // 2. Still not a FaroUser(First name and last name should be null since user still has to register to faro)
        Assert.assertNull(invitees.get("not_a_user@farotest.com").getFirstName());
        Assert.assertNull(invitees.get("not_a_user@farotest.com").getLastName());
        Assert.assertEquals(EventInviteStatus.INVITED, invitees.get("not_a_user@farotest.com").getInviteStatus());
        // 3. Verify owner is part of it and has invite status as accepted
        Assert.assertEquals("sachin", invitees.get(callerEmail).getFirstName());
        Assert.assertEquals("tendulkar", invitees.get(callerEmail).getLastName());
        Assert.assertEquals(EventInviteStatus.ACCEPTED, invitees.get(callerEmail).getInviteStatus());
        
        
        // Update creators status as MAY_BE.. This is just a test. Should not happen in real
        // Since only creators token available, playing around with his status
        ClientResponse updateInviteStatus = TestHelper.doPOST(endpoint.toString(), "v1/event/"+ev.getEventId()+"/updateInviteStatus", token, EventInviteStatus.MAYBE);
        Assert.assertEquals(updateInviteStatus.getStatus(), 200);
        Assert.assertEquals(updateInviteStatus.getEntity(String.class), "OK");
        
        // Verify update.. Can be consistency issue. Run dev server with -Ddatastore.default_high_rep_job_policy_unplied_job_pct=100
        eventInviteesResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ev.getEventId()+"/invitees", new MultivaluedMapImpl(), token);
        eventInvitees = eventInviteesResponse.getEntity(InviteeList.class);
        invitees = eventInvitees.getUserStatusMap();
        Assert.assertEquals(EventInviteStatus.MAYBE, invitees.get(callerEmail).getInviteStatus());
        
        // DECLINE an event
        updateInviteStatus = TestHelper.doPOST(endpoint.toString(), "v1/event/"+ev.getEventId()+"/updateInviteStatus", token, EventInviteStatus.DECLINED);
        Assert.assertEquals(updateInviteStatus.getStatus(), 200);
        Assert.assertEquals(updateInviteStatus.getEntity(String.class), "OK");
        
        // Verify DECLINE.. Can be consistency issue. Run dev server with -Ddatastore.default_high_rep_job_policy_unplied_job_pct=100
        eventInviteesResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ev.getEventId()+"/invitees", new MultivaluedMapImpl(), token);
        eventInvitees = eventInviteesResponse.getEntity(InviteeList.class);
        invitees = eventInvitees.getUserStatusMap();
        // Object itself will not be returned
        Assert.assertNull(invitees.get(callerEmail));
        
        // Remove attendee
        ClientResponse removeAttendee = TestHelper.doPOST(endpoint.toString(), "v1/event/"+ev.getEventId()+"/removeAttendee", token, friendIds[0]);
        Assert.assertEquals(removeAttendee.getStatus(), 200);
        Assert.assertEquals(removeAttendee.getEntity(String.class), "OK");
        
        // Size of all eventInvitees should be 2 less after above deletion and Decline status
        eventInviteesResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ev.getEventId()+"/invitees", new MultivaluedMapImpl(), token);
        eventInviteesResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ev.getEventId()+"/invitees", new MultivaluedMapImpl(), token);
        eventInviteesResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ev.getEventId()+"/invitees", new MultivaluedMapImpl(), token);
        eventInvitees = eventInviteesResponse.getEntity(InviteeList.class);
        invitees = eventInvitees.getUserStatusMap();
        Assert.assertEquals(friends.size()-1, invitees.size());
        
        
    }
    
    // 4. disableEventControl
    public void disableEventControl() throws IOException{
    	Event eventCreateData = new Event("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), false,"Description",  null, new Location("NYC"), "Mafia god");
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event ev = response.getEntity(Event.class);
        ClientResponse response1 = TestHelper.doGET(endpoint.toString(), "v1/event/"+ ev.getEventId()+"/details", new MultivaluedMapImpl(), token);
        Event eventDetails = response1.getEntity(Event.class);
        assertEntity(eventCreateData, eventDetails);
        
        // Disable event control. Returns "OK"
        ClientResponse response2 = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventDetails.getEventId()+"/disableControl", token, eventCreateData);
        String str = response2.getEntity(String.class);
        Assert.assertEquals("OK", str);
        
        // Verify indeed disabled
        ClientResponse verifyControlFlag = TestHelper.doGET(endpoint.toString(), "v1/event/"+ ev.getEventId()+"/details", new MultivaluedMapImpl(), token);
        eventDetails = verifyControlFlag.getEntity(Event.class);
        Assert.assertTrue(eventDetails.getControlFlag());
    }
    
    // 5. removeAttendee
    // *** Covered with getEventInvitees ***
        
    // 6. addFriend
    // *** Covered with getEventInvitees ***
    
    // 7. getEvents
    public void getEvents() throws Exception{
    	// Create 3 events
    	Event eventCreateData = new Event("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), false,"Description",  null, new Location("NYC"), "Mafia god");
    	Event eventCreateData1 = new Event("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), false,"Description",  null, new Location("NYC"), "Mafia god");
    	Event eventCreateData2 = new Event("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), false,"Description",  null, new Location("NYC"), "Mafia god");
    	ClientResponse createEventResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
    	ClientResponse createEventResponse1 = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData1);
    	ClientResponse createEventResponse2 = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData2);
        // get all 3 events
    	ClientResponse eventsResponse = TestHelper.doGET(endpoint.toString(), "v1/events", new MultivaluedMapImpl(), token);
    	eventsResponse = TestHelper.doGET(endpoint.toString(), "v1/events", new MultivaluedMapImpl(), token);
    	eventsResponse = TestHelper.doGET(endpoint.toString(), "v1/events", new MultivaluedMapImpl(), token);
    	List<EventInviteStatusWrapper> events = eventsResponse.getEntity(new GenericType<List<EventInviteStatusWrapper>>(){});
    	Assert.assertEquals(15, events.size());
    }
    
    public void updateEvent() throws Exception{
    	// Create
    	Event eventCreateData = new Event("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), false,"Description",  null, new Location("NYC"), "Mafia god");
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event ev = response.getEntity(Event.class);
    	
    	// Read
    	ClientResponse response1 = TestHelper.doGET(endpoint.toString(), "v1/event/"+ ev.getEventId()+"/details", new MultivaluedMapImpl(), token);
        Event eventDetails = response1.getEntity(Event.class);
        assertEntity(eventCreateData, eventDetails);
        
        // Update
        Event updateObj = new Event();
        updateObj.setEventId(eventDetails.getEventId());
        
        updateObj.setEndDate(Calendar.getInstance());
        updateObj.setEventDescription("Updated description");
        updateObj.setEventName("Updated event name");
        updateObj.setLocation(new Location("Crater Lake"));
        updateObj.setStartDate(Calendar.getInstance());
        updateObj.setStatus(ObjectStatus.CLOSED);
        
        ClientResponse updateResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventDetails.getEventId()+"/updateEvent", token, updateObj);
        ev = updateResponse.getEntity(Event.class);
        Assert.assertEquals(updateObj.getEndDate(), ev.getEndDate());
        Assert.assertEquals(updateObj.getStartDate(), ev.getStartDate());
        Assert.assertEquals(updateObj.getEventDescription(), ev.getEventDescription());
        Assert.assertEquals(updateObj.getEventName(), ev.getEventName());
        Assert.assertEquals(updateObj.getStatus(), ev.getStatus());
        Assert.assertEquals(updateObj.getLocation().locationName, ev.getLocation().locationName);
    }
    
    @Test
    public void allTest() throws Exception{
    	System.out.println(token);
    	createEventTest();
    	getEventDetails();
    	disableEventControl();
    	getEvents();
    	getEventInvitees();
    	updateEvent();
    }

    
    public static void assertEntity(Event expected, Event actual){
    	Assert.assertEquals(expected.getEventName(), actual.getEventName());
        Assert.assertEquals(expected.getEndDate(), actual.getEndDate());
        Assert.assertEquals(expected.getStartDate(), actual.getStartDate());
        Assert.assertEquals(expected.getLocation().locationName, actual.getLocation().locationName);
        Assert.assertNotNull(actual.getEventId());
        Assert.assertTrue(!actual.getControlFlag());
        Assert.assertNotNull(actual.getAssignment());
        Assert.assertNotNull(actual.getAssignment().getId());
        Assert.assertEquals(expected.getEventDescription(), actual.getEventDescription());
        Assert.assertNotNull(actual.getEventCreatorId());
        Assert.assertEquals(ObjectStatus.OPEN, actual.getStatus());
    }
    
}
