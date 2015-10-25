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
import com.zik.faro.api.bean.Event;
import com.zik.faro.api.responder.AddFriendRequest;
import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.api.responder.InviteeList;
import com.zik.faro.api.responder.InviteeList.Invitees;
import com.zik.faro.data.Location;

public class FunctionalEventTest {
    private static URL endpoint;
    private static String token = null;

    @BeforeClass
    public static void init() throws Exception {
        endpoint = TestHelper.getExternalTargetEndpoint();
        token = TestHelper.createUserAndGetToken();
    }
    
    
    // **** Tests ****
    
    // 1. createEvent
    public void createEventTest() throws Exception {
    	EventCreateData eventCreateData = new EventCreateData("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), new Location("Random Location"), null);
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event event = response.getEntity(Event.class);
        assertEntity(eventCreateData, event);
    }
    
    // 2. getEventDetails
    public void getEventDetails() throws Exception{
    	EventCreateData eventCreateData = new EventCreateData("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), new Location("NYC"), null);
    	ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event ev = response.getEntity(Event.class);
        ClientResponse response1 = TestHelper.doGET(endpoint.toString(), "v1/event/"+ ev.getEventId()+"/details", new MultivaluedMapImpl(), token);
        Event eventDetails = response1.getEntity(Event.class);
        assertEntity(eventCreateData, eventDetails);
    }
    
    // 3. getEventInvitees
    public void getEventInvitees() throws Exception{
    	// Create event
    	EventCreateData eventCreateData = new EventCreateData("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), new Location("NYC"), null);
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
        
        // Remove attendee
        ClientResponse removeAttendee = TestHelper.doPOST(endpoint.toString(), "v1/event/"+ev.getEventId()+"/removeAttendee", token, friendIds[0]);
        Assert.assertEquals(removeAttendee.getStatus(), 200);
        Assert.assertEquals(removeAttendee.getEntity(String.class), "OK");
        
        // Size of all eventInvitees should be 1 less after above deletion
        eventInviteesResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ev.getEventId()+"/invitees", new MultivaluedMapImpl(), token);
        eventInviteesResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ev.getEventId()+"/invitees", new MultivaluedMapImpl(), token);
        eventInviteesResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+ev.getEventId()+"/invitees", new MultivaluedMapImpl(), token);
        eventInvitees = eventInviteesResponse.getEntity(InviteeList.class);
        invitees = eventInvitees.getUserStatusMap();
        Assert.assertEquals(friends.size(), invitees.size());
        
        
    }
    
    // 4. disableEventControl
    public void disableEventControl() throws IOException{
    	EventCreateData eventCreateData = new EventCreateData("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), new Location("NYC"), null);
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
    	EventCreateData eventCreateData = new EventCreateData("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), new Location("NYC"), null);
    	EventCreateData eventCreateData1 = new EventCreateData("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), new Location("SFO"), null);
    	EventCreateData eventCreateData2 = new EventCreateData("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), new Location("Vegas"), null);
    	ClientResponse createEventResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
    	ClientResponse createEventResponse1 = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData1);
    	ClientResponse createEventResponse2 = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData2);
        // get all 3 events
    	ClientResponse eventsResponse = TestHelper.doGET(endpoint.toString(), "v1/events", new MultivaluedMapImpl(), token);
    	eventsResponse = TestHelper.doGET(endpoint.toString(), "v1/events", new MultivaluedMapImpl(), token);
    	eventsResponse = TestHelper.doGET(endpoint.toString(), "v1/events", new MultivaluedMapImpl(), token);
    	List<Event> events = eventsResponse.getEntity(new GenericType<List<Event>>(){});
    	Assert.assertEquals(6, events.size());
    }
    
    @Test
    public void allTest() throws Exception{
    	createEventTest();
    	getEventDetails();
    	disableEventControl();
    	getEvents();
    	getEventInvitees();
    }

    
    public static void assertEntity(EventCreateData expected, Event actual){
    	Assert.assertEquals(expected.getEventName(), actual.getEventName());
        Assert.assertEquals(expected.getEndDate(), actual.getEndDate());
        Assert.assertEquals(expected.getStartDate(), actual.getStartDate());
        Assert.assertEquals(expected.getLocation().locationName, actual.getLocation().locationName);
        Assert.assertNotNull(actual.getEventId());
        Assert.assertTrue(!actual.getControlFlag());
    }
    
}
