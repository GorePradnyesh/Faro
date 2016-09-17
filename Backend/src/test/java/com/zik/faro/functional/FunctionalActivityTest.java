package com.zik.faro.functional;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.zik.faro.TestHelper;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.Item;
import com.zik.faro.data.Location;
import com.zik.faro.data.Unit;
import com.zik.faro.data.expense.ExpenseGroup;

public class FunctionalActivityTest {
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
    	Event eventCreateData = new Event("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), false, "Description", null, new Location("Random Location"), "Mafia god");
    	
        ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event event = response.getEntity(Event.class);
        FunctionalEventTest.assertEntity(eventCreateData, event);
        eventId = event.getEventId();
    }

    @SuppressWarnings("deprecation")
    private static void assertEntity(Activity activityRequest, Activity activityResponse){
        Assert.assertNotNull(activityResponse.getId());
        Assert.assertEquals(activityRequest.getEventId(), activityResponse.getEventId());
        Assert.assertEquals(activityRequest.getDescription(), activityResponse.getDescription());
        Assert.assertEquals(activityRequest.getName(), activityResponse.getName());
        Assert.assertEquals(activityRequest.getStartDate(), activityResponse.getStartDate());
        Assert.assertEquals(activityRequest.getEndDate(), activityResponse.getEndDate());
        Assert.assertEquals(activityRequest.getLocation().locationName, activityResponse.getLocation().locationName);
//        for(int i = 0 ; i < activityRequest.getAssignment().getItems().size() ; i++){
//            Assert.assertEquals(activityRequest.getAssignment().getItems().get(i).getName(), activityResponse.getAssignment().getItems().get(i).getName());
//            Assert.assertEquals(activityRequest.getAssignment().getItems().get(i).getCount(), activityResponse.getAssignment().getItems().get(i).getCount());
//            Assert.assertEquals(activityRequest.getAssignment().getItems().get(i).getAssigneeId(), activityResponse.getAssignment().getItems().get(i).getAssigneeId());
//            Assert.assertEquals(activityRequest.getAssignment().getItems().get(i).getId(), activityResponse.getAssignment().getItems().get(i).getId());
//            Assert.assertEquals(activityRequest.getAssignment().getItems().get(i).getStatus(), activityResponse.getAssignment().getItems().get(i).getStatus());
//        }
//        Assert.assertEquals(activityRequest.getAssignment().getId(), activityResponse.getAssignment().getId());
//        Assert.assertEquals(activityRequest.getAssignment().getStatus(), activityResponse.getAssignment().getStatus());
    }
    
    @SuppressWarnings("deprecation")
    public static void assertCreatedEntity(Activity activityRequest, Activity activityResponse){
        Assert.assertNotNull(activityResponse.getId());
        Assert.assertEquals(activityRequest.getEventId(), activityResponse.getEventId());
        Assert.assertEquals(activityRequest.getDescription(), activityResponse.getDescription());
        Assert.assertEquals(activityRequest.getName(), activityResponse.getName());
        Assert.assertEquals(activityRequest.getStartDate(), activityResponse.getStartDate());
        Assert.assertEquals(activityRequest.getEndDate(), activityResponse.getEndDate());
        Assert.assertEquals(activityRequest.getLocation().locationName, activityResponse.getLocation().locationName);
        Assert.assertNotNull(activityResponse.getAssignment());
        Assert.assertNotNull(activityResponse.getAssignment().getId());
    }

    public static void createAndReadActivityTest() throws Exception{
//        Assignment assignment = new Assignment("HikingToDo", ActionStatus.INCOMPLETE);
//        assignment.addItem(new Item("food", "Gaurav", 2, Unit.COUNT));
//        assignment.addItem(new Item("drinks", "Paddy", 5, Unit.COUNT));
        // Create sample activity without id
        Activity activity = new Activity(eventId, "Hiking",
                "Test activity description", new Location("NYC"),
                Calendar.getInstance(), Calendar.getInstance(),null);
        ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/activity/create", token, activity);
        Activity activityResponse = response.getEntity(Activity.class);
        assertCreatedEntity(activity, activityResponse);
        // Read recently created activity
        ClientResponse readResponse = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/activity/"+activityResponse.getId(), new MultivaluedMapImpl(), token);
        activityResponse = readResponse.getEntity(Activity.class);
        assertCreatedEntity(activity, activityResponse);
    }

    public static void updateActivityTest()throws Exception{
//        Assignment assignment = new Assignment("HikingToDo", ActionStatus.INCOMPLETE);
//        assignment.addItem(new Item("food", "Gaurav", 2, Unit.COUNT));
//        assignment.addItem(new Item("drinks", "Paddy", 5, Unit.COUNT));
        // Create sample activity without id
        Activity activity = new Activity(eventId, "Hiking",
                "Test activity description", new Location("NYC"),
                Calendar.getInstance(), Calendar.getInstance(), null);
        ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/activity/create", token, activity);
        Activity activityResponse = response.getEntity(Activity.class);
        assertCreatedEntity(activity, activityResponse);
        // Update activity. Activity id and event id should be passed in URI. Values in activity object will not be honored
        activity.setDescription("Update activity description");
        activity.setStartDate(Calendar.getInstance());
        activity.setEndDate(Calendar.getInstance());
        activity.setLocation(new Location("SFO"));
//        activity.getAssignment().getItems().remove("food");
//        activity.getAssignment().addItem(new Item("hookah", "Kunal", 1, Unit.COUNT));

        ClientResponse updateResponse = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/activity/"+ activityResponse.getId()+"/update", token, activity);
        activityResponse = updateResponse.getEntity(Activity.class);
        assertCreatedEntity(activity, activityResponse);
    }

    public static void deleteActivityTest() throws Exception{
//        Assignment assignment = new Assignment("HikingToDo", ActionStatus.INCOMPLETE);
//        assignment.addItem(new Item("food", "Gaurav", 2, Unit.COUNT));
//        assignment.addItem(new Item("drinks", "Paddy", 5, Unit.COUNT));
        // Create sample activity without id
        Activity activity = new Activity(eventId, "Hiking",
                "Test activity description", new Location("NYC"),
                Calendar.getInstance(), Calendar.getInstance(), null);
        ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/activity/create", token, activity);
        Activity activityResponse = response.getEntity(Activity.class);
        assertCreatedEntity(activity, activityResponse);

        // Invoke delete api
        response = TestHelper.doDELETE(endpoint.toString(), "v1/event/"+eventId+"/activity/"+ activityResponse.getId(), token);
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.getEntity(String.class), "OK");

        // Read and check indeed deleted
        response = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/activity/"+activityResponse.getId(), new MultivaluedMapImpl(), token);
        Assert.assertEquals(response.getStatus(), 404);
        String notFoundResponse = response.getEntity(String.class);
        Assert.assertTrue(notFoundResponse.contains("Data not found"));
    }

    public static void getActivitiesTest() throws Exception{
//        Assignment assignment = new Assignment("HikingToDo", ActionStatus.INCOMPLETE);
//        assignment.addItem(new Item("food", "Gaurav", 2, Unit.COUNT));
//        assignment.addItem(new Item("drinks", "Paddy", 5, Unit.COUNT));
        // Create sample activity without id
        Activity activity = new Activity(eventId, "Hiking",
                "Test activity description", new Location("NYC"),
                Calendar.getInstance(), Calendar.getInstance(), null);
        ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/activity/create", token, activity);
        Activity activityResponse = response.getEntity(Activity.class);
        assertCreatedEntity(activity, activityResponse);
        // Create another activity
        Activity activity1 = new Activity(eventId, "Swimming",
                "Test activity description for swimming", new Location("Mafatlal bath"),
                Calendar.getInstance(), Calendar.getInstance(), null);
        response = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/activity/create", token, activity1);
        Activity activityResponse1 = response.getEntity(Activity.class);
        assertCreatedEntity(activity1, activityResponse1);

        // Read both above created activities
        response = TestHelper.doGET(endpoint.toString(), "v1/event/"+eventId+"/activities", new MultivaluedMapImpl(), token);
        List<Activity> list = response.getEntity(new GenericType<List<Activity>>(){});
        for(int i = 0 ; i < list.size() ; i++){
            Activity ac = (Activity)list.get(0);
            if(ac.getId().equals(activityResponse.getId())){
            	assertCreatedEntity(activityResponse, ac);
            }else{
            	assertCreatedEntity(activityResponse1, ac);
            }
        }
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void allTest() throws Exception{
        getActivitiesTest();
        createAndReadActivityTest();
        updateActivityTest();
        deleteActivityTest();

    }

}