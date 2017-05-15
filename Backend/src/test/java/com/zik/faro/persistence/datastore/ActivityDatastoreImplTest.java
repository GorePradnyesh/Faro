package com.zik.faro.persistence.datastore;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.data.Assignment;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.data.Item;
import com.zik.faro.data.Location;
import com.zik.faro.data.GeoPosition;
import com.zik.faro.data.Unit;


public class ActivityDatastoreImplTest {

	private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));
	
    static{
    	ObjectifyService.register(ActivityDo.class);
        ObjectifyService.register(EventDo.class);
        
    }

    @BeforeClass
    public static void init(){
        ObjectifyService.begin();       // This is needed to set up the ofy service.
    }

    @Before
    public void setUp() {
        helper.setUp();
    }

    @AfterClass
    public static void tearDown() {
        helper.tearDown();
    }


    @Test
    public void loadActivityByIdTest() throws IllegalDataOperation, DataNotFoundException{
        final String testEventId = "Event1";

        /*Create and store test activity 1*/
        ActivityDo activity1 = createActivity("123", "dummy1");
        
        ActivityDatastoreImpl.storeActivity(activity1);

        ActivityDo retrievedActivity = ActivityDatastoreImpl.loadActivityById(activity1.getId(), activity1.getEventId());
        Assert.assertEquals(activity1.getId(), retrievedActivity.getId());
        Assert.assertEquals(activity1.getAssignment().getId(), retrievedActivity.getAssignment().getId());

        /*Create and store new Activity for the same event ( but of course with different ID) */
        ActivityDo activity2 = createActivity("123", "dummy2");
        ActivityDatastoreImpl.storeActivity(activity2);

        /*Load the activities for testEventID and verify that both activities are retrieved*/
        List<ActivityDo> activityList = ActivityDatastoreImpl.loadActivitiesByEventId(activity1.getEventId());
        Assert.assertEquals(2, activityList.size());
    }
    
    private ActivityDo createActivity(String eventId, String name) throws IllegalDataOperation{
		GeoPosition geoPosition = new GeoPosition(0,0);
    	ActivityDo activity1 = new ActivityDo(eventId, name, "dummyDescription",
                new Location("Lake Shasta", "Lake Shasta's Address", geoPosition),
                new GregorianCalendar(),
                new GregorianCalendar(),
                new Assignment());
        Assignment tempAssignment = new Assignment();
        tempAssignment.addItem(new Item("blankets", "David", 4, Unit.COUNT));
        tempAssignment.addItem(new Item("rice", "Roger", 10, Unit.LB));
        activity1.setAssignment(tempAssignment);
        return activity1;
    }
    
    @Test
    public void loadActivitiesByEventIdTest() throws IllegalDataOperation{
    	/*Create and store test activity 1*/
    	ActivityDo activity1 = createActivity("345", "NewYork");
    	ActivityDo activity2 = createActivity("345", "SF");
    	ActivityDo activity3 = createActivity("345", "Buffalo");
        
    	ActivityDatastoreImpl.storeActivity(activity1);
    	ActivityDatastoreImpl.storeActivity(activity2);
    	ActivityDatastoreImpl.storeActivity(activity3);
    	
    	List<ActivityDo> list = ActivityDatastoreImpl.loadActivitiesByEventId(activity1.getEventId());
    	Assert.assertEquals(list.size(), 3);
    	
    }
    
    @Test
    public void deleteActivityById() throws IllegalDataOperation, DataNotFoundException{
    	// First create
    	ActivityDo a = createActivity("12345", "Snowboarding");
    	ActivityDatastoreImpl.storeActivity(a);
    	
    	// Verify indeed created
    	ActivityDo retrievedActivity = ActivityDatastoreImpl.loadActivityById(a.getId(), a.getEventId());
    	Assert.assertNotNull(retrievedActivity);
    	
    	//ActivityDatastoreImpl
    	ActivityDatastoreImpl.delelteActivityById(a.getId(), a.getEventId());
    	
    	// Verify deleted
    	try{
    		retrievedActivity = ActivityDatastoreImpl.loadActivityById(a.getId(), a.getEventId());
    	}catch(DataNotFoundException e){
    		retrievedActivity = null;
    	}
    	Assert.assertNull(retrievedActivity);
    }
    
    @Test
    public void testUpdateActivity() throws IllegalDataOperation, DataNotFoundException, DatastoreException, UpdateVersionException{
    	// Create activity
    	EventDo event = new EventDo("TestEvent");
    	String eventId = event.getId();
    	EventDatastoreImpl.storeEventOnly(event);
		GeoPosition geoPosition1 = new GeoPosition(0,0);
		GeoPosition geoPosition2 = new GeoPosition(100,100);
    	
    	ActivityDo a = new ActivityDo(eventId, "TestEvent", "Testing update",
    			new Location("San Jose", "CA", geoPosition1),new GregorianCalendar(), new GregorianCalendar(), new Assignment());
    	ActivityDatastoreImpl.storeActivity(a);
    	
    	// Verify indeed created
    	ActivityDo retrievedActivity = ActivityDatastoreImpl.loadActivityById(a.getId(), eventId);
    	Assert.assertNotNull(retrievedActivity);
    	// Modify
    	a.setStartDate(new GregorianCalendar());
    	a.setEndDate(new GregorianCalendar());
    	a.setDescription("Description changed");
    	a.setLocation(new Location("Fremont", "CA", geoPosition2));
    	
    	// Update
    	ActivityDatastoreImpl.updateActivity(a, eventId);
    	retrievedActivity = ActivityDatastoreImpl.loadActivityById(a.getId(), eventId);
    	Assert.assertNotNull(retrievedActivity);
    	
    	// Verify
    	Assert.assertEquals(retrievedActivity.getLocation().getLocationName(), "Fremont");
    	Assert.assertEquals(retrievedActivity.getLocation().getLocationAddress(), "CA");
    	Assert.assertEquals(retrievedActivity.getLocation().getPosition(), geoPosition2);
    	Assert.assertEquals(retrievedActivity.getDescription(), "Description changed");
    	
    	// Test update with correct version
    	a.setStartDate(Calendar.getInstance());
    	a.setVersion(retrievedActivity.getVersion());
    	ActivityDatastoreImpl.updateActivity(a, eventId);
    	retrievedActivity = ActivityDatastoreImpl.loadActivityById(a.getId(), eventId);
    	Assert.assertNotNull(retrievedActivity);
    	Assert.assertEquals(a.getStartDate(), retrievedActivity.getStartDate());
    	
    	// Test update failure without correct version
    	a.setStartDate(Calendar.getInstance());
    	try{
    		ActivityDatastoreImpl.updateActivity(a, eventId);
    	} catch(UpdateVersionException e){
    		Assert.assertNotNull(e);
    		Assert.assertEquals(e.getMessage(), "Incorrect entity version. Current version:3");
    		return;
    	}
    	// Execution should never reach here
    	Assert.assertNull("Activity update failure");
    }
  
}
