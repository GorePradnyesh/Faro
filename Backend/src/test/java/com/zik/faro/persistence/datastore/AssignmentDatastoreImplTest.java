package com.zik.faro.persistence.datastore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.data.Assignment;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.data.Identifier;
import com.zik.faro.data.Item;
import com.zik.faro.data.Location;
import com.zik.faro.data.Unit;

public class AssignmentDatastoreImplTest {
	
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
    
	private ActivityDo createActivity(String eventId, String name) throws IllegalDataOperation{
    	ActivityDo activity1 = new ActivityDo(eventId, name, "dummyDescription",
                new Location("Lake Shasta"),
                new GregorianCalendar(),
                new GregorianCalendar(),
                new Assignment());
        Assignment tempAssignment = new Assignment();
        tempAssignment.addItem(new Item("blankets", "David", 4, Unit.COUNT, Identifier.createUniqueIdentifierString()));
        tempAssignment.addItem(new Item("rice", "Roger", 10, Unit.LB, Identifier.createUniqueIdentifierString()));
        activity1.setAssignment(tempAssignment);
        return activity1;
    }
	
	@Test
	public void getAllAssignmentsTest() throws IllegalDataOperation, DataNotFoundException{
		EventDo event = new EventDo("TestEvent", Calendar.getInstance(), Calendar.getInstance(), false, null, new Location("San Jose"));
    	EventDatastoreImpl.storeEventOnly(event);
		ActivityDo activity1 = createActivity(event.getEventId(), "NewYork");
    	ActivityDo activity2 = createActivity(event.getEventId(), "SF");
    	ActivityDo activity3 = createActivity(event.getEventId(), "Buffalo");
        
    	ActivityDatastoreImpl.storeActivity(activity1);
    	ActivityDatastoreImpl.storeActivity(activity2);
    	ActivityDatastoreImpl.storeActivity(activity3);
    	
    	Map<String,Assignment> assignments = AssignmentDatastoreImpl.getAllAssignments(event.getEventId());
    	Assert.assertEquals(4, assignments.size());
    }
	
	@Test
	public void getAssignmentUsingEvent_Activity_Assignment_IdTest() throws DataNotFoundException, IllegalDataOperation{
		EventDo event = new EventDo("TestEvent", Calendar.getInstance(), Calendar.getInstance(), false, null, new Location("San Jose"));
    	EventDatastoreImpl.storeEventOnly(event);
		ActivityDo activity1 = createActivity(event.getEventId(), "NewYork");
		ActivityDatastoreImpl.storeActivity(activity1);
		Assignment assignment = AssignmentDatastoreImpl.getActivityAssignment(event.getEventId(), activity1.getId(), 
				activity1.getAssignment().getId());
		Assert.assertEquals(assignment.getId(), activity1.getAssignment().getId());
	}


/*	@Test
	public void getAssignmentUsingEvent_Assignment_IdTest() throws DataNotFoundException, IllegalDataOperation{
		Event event = new Event("TestEvent", new GregorianCalendar(), new GregorianCalendar(), false, null, new Location("San Jose"));
    	EventDatastoreImpl.storeEvent(event);
		Activity activity1 = createActivity(event.getEventId(), "NewYork");
    	Activity activity2 = createActivity(event.getEventId(), "SF");
    	Activity activity3 = createActivity(event.getEventId(), "Buffalo");
        
    	ActivityDatastoreImpl.storeActivity(activity1);
    	ActivityDatastoreImpl.storeActivity(activity2);
    	ActivityDatastoreImpl.storeActivity(activity3);
    	
    	Assignment assignment = AssignmentDatastoreImpl.getActivityAssignment(event.getEventId(),
    			activity2.getAssignment().id);
    	Assert.assertEquals(assignment.id, activity2.getAssignment().id);
	}*/
	
	@Test
	public void getCountOfPendingAssignmentsTest() throws IllegalDataOperation, DataNotFoundException{
		EventDo event = new EventDo("TestEvent", Calendar.getInstance(), Calendar.getInstance(), false, null, new Location("San Jose"));
    	EventDatastoreImpl.storeEventOnly(event);
		ActivityDo activity1 = createActivity(event.getEventId(), "NewYork");
    	ActivityDo activity2 = createActivity(event.getEventId(), "SF");
    	activity1.getAssignment().getItems().get(0).setStatus(ActionStatus.COMPLETE);
    	activity1.getAssignment().getItems().get(1).setStatus(ActionStatus.INCOMPLETE);
    	activity2.getAssignment().getItems().get(0).setStatus(ActionStatus.INCOMPLETE);
    	activity2.getAssignment().getItems().get(1).setStatus(ActionStatus.INCOMPLETE);
    	
    	ActivityDatastoreImpl.storeActivity(activity1);
    	ActivityDatastoreImpl.storeActivity(activity2);
    	int count = AssignmentDatastoreImpl.getCountOfPendingAssignments(event.getEventId());
    	Assert.assertEquals(3, count);
	}
	
	@Test
	public void updateActivityLevelAssignmentItems() throws IllegalDataOperation, DataNotFoundException, DatastoreException{
		EventDo event = new EventDo("TestEvent", Calendar.getInstance(), Calendar.getInstance(), false, null, new Location("San Jose"));
    	EventDatastoreImpl.storeEventOnly(event);
		ActivityDo activity1 = createActivity(event.getEventId(), "NewYork");
		ActivityDatastoreImpl.storeActivity(activity1);
    	List<Item> items = getItems(activity1.getAssignment());
    	
    	AssignmentDatastoreImpl.updateItemsForActivityAssignment(event.getEventId(),
    			activity1.getId(), items);
    	Assignment assignment = AssignmentDatastoreImpl.getActivityAssignment(event.getEventId(),
    			activity1.getId(), activity1.getAssignment().getId());
    	// Assert size, 1 newly added and 1 updated item
    	Assert.assertEquals(4,assignment.getItems().size());
    	//Assert.assertEquals(items.get(2), assignment.getItem(items.get(3).getId()));
    	//Assert.assertEquals(items.get(1), assignment.getItem(items.get(1).getId()));
    	
	}
	
	private List<Item> getItems(Assignment assignment) throws IllegalDataOperation{
		List<Item> items = new ArrayList<Item>();
		items.add(new Item("Tomatoes", "12345", 1, Unit.KG, Identifier.createUniqueIdentifierString()));
		items.add(new Item("Onions", "123456", 1, Unit.KG, Identifier.createUniqueIdentifierString()));
		items.add(new Item("TODO name changed", "David", 4, Unit.COUNT, assignment.getItems().get(0).getId()));
		return items;
	}
	
	@Test
	public void updateEventLevelAssignmentItems() throws IllegalDataOperation, DataNotFoundException, DatastoreException{
		EventDo event = new EventDo("TestEvent", Calendar.getInstance(), Calendar.getInstance(), false, null, new Location("San Jose"));
		Assignment eventAssignment = new Assignment();
		eventAssignment.addItem(new Item("Kaivan", "420", 3, Unit.KG, Identifier.createUniqueIdentifierString()));
		event.setAssignment(eventAssignment);
    	EventDatastoreImpl.storeEventOnly(event);
		List<Item> items = getItems(event.getAssignment());
    	
		AssignmentDatastoreImpl.updateItemsForEventAssignment(event.getEventId(),items);
    	Assignment assignment = AssignmentDatastoreImpl.getEventAssignment(event.getEventId());
    	// Assert size, 1 newly added and 1 updated item
    	Assert.assertEquals(3,assignment.getItems().size());
    	//Assert.assertEquals(items.get(2), assignment.getItem(items.get(2).getId()));
    	//Assert.assertEquals(items.get(1), assignment.getItem(items.get(1).getId()));
	}
	
	
}
