package com.zik.faro.persistence.datastore;

import java.util.GregorianCalendar;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.google.appengine.repackaged.org.joda.time.ReadableInstant;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.IllegalDataOperation;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.ActivityDo;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.EventDo;
import com.zik.faro.data.Item;
import com.zik.faro.data.Location;
import com.zik.faro.data.Unit;


public class ActivityDatastoreImplTest {

	private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(50));
	
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
    	ActivityDo activity1 = new ActivityDo(eventId, name, "dummyDescription",
                new Location("Lake Shasta"),
                new GregorianCalendar(),
                new Assignment("1234",ActionStatus.INCOMPLETE));
        Assignment tempAssignment = new Assignment("12345", ActionStatus.INCOMPLETE);
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
    public void testUpdateActivity() throws IllegalDataOperation, DataNotFoundException{
    	// Create activity
    	EventDo event = new EventDo("TestEvent");
    	String eventId = event.getEventId();
    	EventDatastoreImpl.storeEventOnly(event);
    	
    	ActivityDo a = new ActivityDo(eventId, "TestEvent", "Testing update",
    			new Location("San Jose"),new GregorianCalendar(), new Assignment("12", ActionStatus.INCOMPLETE));
    	ActivityDatastoreImpl.storeActivity(a);
    	
    	// Verify indeed created
    	ActivityDo retrievedActivity = ActivityDatastoreImpl.loadActivityById(a.getId(), eventId);
    	Assert.assertNotNull(retrievedActivity);
    	// Modify
    	a.getAssignment().addItem(new Item("Test", "123", 1, Unit.CENTIMETER));
    	a.setDate(new GregorianCalendar());
    	a.setDescription("Description changed");
    	a.setLocation(new Location("Fremont"));
    	
    	// Update
    	ActivityDatastoreImpl.updateActivity(a, eventId);
    	retrievedActivity = ActivityDatastoreImpl.loadActivityById(a.getId(), eventId);
    	Assert.assertNotNull(retrievedActivity);
    	
    	// Verify
    	Assert.assertEquals(retrievedActivity.getAssignment().getItemsList().get(0).getAssigneeId(), "123");
    	Assert.assertEquals(retrievedActivity.getLocation().locationName, "Fremont");
    	Assert.assertEquals(retrievedActivity.getDescription(), "Description changed");
    	
    }
  
}
