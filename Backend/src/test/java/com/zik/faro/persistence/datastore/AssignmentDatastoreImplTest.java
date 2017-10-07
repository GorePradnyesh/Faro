package com.zik.faro.persistence.datastore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.collections.ListUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.TestUtil;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.GeoPosition;
import com.zik.faro.data.Identifier;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.Item;
import com.zik.faro.data.Location;
import com.zik.faro.data.Unit;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.EventDo;

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
		GeoPosition geoPosition = new GeoPosition(0,0);
    	ActivityDo activity1 = new ActivityDo(eventId, name, "dummyDescription",
                new Location("Lake Shasta", "Lake Shasta's Address", geoPosition),
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
		GeoPosition geoPosition = new GeoPosition(0,0);
		EventDo event = new EventDo("TestEvent", Calendar.getInstance(),
				Calendar.getInstance(), false, null, new Location("San Jose", "CA", geoPosition));
    	EventDatastoreImpl.storeEventOnly(event);
		ActivityDo activity1 = createActivity(event.getId(), "NewYork");
    	ActivityDo activity2 = createActivity(event.getId(), "SF");
    	ActivityDo activity3 = createActivity(event.getId(), "Buffalo");
        
    	ActivityDatastoreImpl.storeActivity(activity1);
    	ActivityDatastoreImpl.storeActivity(activity2);
    	ActivityDatastoreImpl.storeActivity(activity3);
    	
    	Map<String,Assignment> assignments = AssignmentDatastoreImpl.getAllAssignments(event.getId());
    	Assert.assertEquals(4, assignments.size());
    }
	
	@Test
	public void getAssignmentUsingEvent_Activity_Assignment_IdTest() throws DataNotFoundException, IllegalDataOperation{
		GeoPosition geoPosition = new GeoPosition(0,0);
		EventDo event = new EventDo("TestEvent", Calendar.getInstance(), 
				Calendar.getInstance(), false, null, new Location("San Jose", "CA", geoPosition));
    	EventDatastoreImpl.storeEventOnly(event);
		ActivityDo activity1 = createActivity(event.getId(), "NewYork");
		ActivityDatastoreImpl.storeActivity(activity1);
		Assignment assignment = AssignmentDatastoreImpl.getActivityAssignment(event.getId(), activity1.getId(), 
				activity1.getAssignment().getId());
		Assert.assertEquals(assignment.getId(), activity1.getAssignment().getId());
	}


/*	@Test
	public void getAssignmentUsingEvent_Assignment_IdTest() throws DataNotFoundException, IllegalDataOperation{
		GeoPosition geoPosition = new GeoPosition(0,0);
		Event event = new Event("TestEvent", new GregorianCalendar(), 
				new GregorianCalendar(), false, null, new Location("San Jose", "CA", geoPosition));
    	EventDatastoreImpl.storeEvent(event);
		Activity activity1 = createActivity(event.getId(), "NewYork");
    	Activity activity2 = createActivity(event.getId(), "SF");
    	Activity activity3 = createActivity(event.getId(), "Buffalo");
        
    	ActivityDatastoreImpl.storeActivity(activity1);
    	ActivityDatastoreImpl.storeActivity(activity2);
    	ActivityDatastoreImpl.storeActivity(activity3);
    	
    	Assignment assignment = AssignmentDatastoreImpl.getActivityAssignment(event.getId(),
    			activity2.getAssignment().id);
    	Assert.assertEquals(assignment.id, activity2.getAssignment().id);
	}*/
	
	@Test
	public void getCountOfPendingAssignmentsTest() throws IllegalDataOperation, DataNotFoundException{
		GeoPosition geoPosition = new GeoPosition(0,0);
		EventDo event = new EventDo("TestEvent", Calendar.getInstance(),
				Calendar.getInstance(), false, null, new Location("San Jose", "CA", geoPosition));
    	EventDatastoreImpl.storeEventOnly(event);
		ActivityDo activity1 = createActivity(event.getId(), "NewYork");
    	ActivityDo activity2 = createActivity(event.getId(), "SF");
    	activity1.getAssignment().getItems().get(0).setStatus(ActionStatus.COMPLETE);
    	activity1.getAssignment().getItems().get(1).setStatus(ActionStatus.INCOMPLETE);
    	activity2.getAssignment().getItems().get(0).setStatus(ActionStatus.INCOMPLETE);
    	activity2.getAssignment().getItems().get(1).setStatus(ActionStatus.INCOMPLETE);
    	
    	ActivityDatastoreImpl.storeActivity(activity1);
    	ActivityDatastoreImpl.storeActivity(activity2);
    	int count = AssignmentDatastoreImpl.getCountOfPendingAssignments(event.getId());
    	Assert.assertEquals(3, count);
	}
	
	@Test
	public void updateActivityLevelAssignmentItems() throws IllegalDataOperation, DataNotFoundException, DatastoreException, UpdateVersionException, UpdateException{
		GeoPosition geoPosition = new GeoPosition(0,0);
		EventDo event = new EventDo("TestEvent", Calendar.getInstance(), 
				Calendar.getInstance(), false, null, new Location("San Jose", "CA", geoPosition));
    	EventDatastoreImpl.storeEventOnly(event);
		ActivityDo activity1 = createActivity(event.getId(), "NewYork");
		ActivityDatastoreImpl.storeActivity(activity1);
		List<Item> toBeAdded = new ArrayList<>();
		List<Item> toBeRemoved = new ArrayList<>();
	
		// Add new item
		Item i1 = new Item("Onions", "9876543", 1, Unit.KG, Identifier.createUniqueIdentifierString());
		toBeAdded.add(i1);
		ActivityDo updatedActivity = AssignmentDatastoreImpl.updateActivityAssignmentItems(toBeAdded, null, activity1.getId(), activity1.getEventId(), activity1.getVersion());
		
		List<Item> expected = ListUtils.union(activity1.getAssignment().getItems(), toBeAdded);
		Assert.assertTrue(ListUtils.isEqualList(expected, updatedActivity.getAssignment().getItems()));
		
		// Add and update and remove
		toBeAdded.clear();
		i1 = new Item("Tent", "Nakul", 1, Unit.KG, Identifier.createUniqueIdentifierString());
		toBeAdded.add(i1);
		// Update existing item
		updatedActivity.getAssignment().getItems().get(0).setCount(10);
		toBeAdded.add(updatedActivity.getAssignment().getItems().get(0));
		// Remove
		toBeRemoved.add(updatedActivity.getAssignment().getItems().get(2));
		
		ActivityDo updatedActivity1 = AssignmentDatastoreImpl.updateActivityAssignmentItems(toBeAdded, toBeRemoved,
				updatedActivity.getId(), updatedActivity.getEventId(), updatedActivity.getVersion());
		expected = new ArrayList<Item>();
		
		// Handpicking since "2" was removed
		expected.add(updatedActivity.getAssignment().getItems().get(1));
		expected.addAll(toBeAdded);
		Assert.assertEquals(toBeAdded.get(1).getCount(), updatedActivity1.getAssignment().getItems().get(0).getCount());
		Assert.assertTrue(TestUtil.isEqualList(expected, updatedActivity1.getAssignment().getItems()));		
		
		// Only remove
		toBeRemoved.clear();
		toBeRemoved.add(updatedActivity1.getAssignment().getItems().get(0));
		toBeRemoved.add(updatedActivity1.getAssignment().getItems().get(1));
    	
		ActivityDo updatedActivity2 = AssignmentDatastoreImpl.updateActivityAssignmentItems(null, toBeRemoved,
				updatedActivity1.getId(), updatedActivity1.getEventId(), updatedActivity1.getVersion());
    	expected.clear();
    	expected.add(updatedActivity1.getAssignment().getItems().get(2));
    	Assert.assertTrue(TestUtil.isEqualList(expected, updatedActivity2.getAssignment().getItems()));		
    }
	
	
	
	private List<Item> getItems(Assignment assignment) throws IllegalDataOperation{
		List<Item> items = new ArrayList<Item>();
		items.add(new Item("Tomatoes", "12345", 1, Unit.KG, Identifier.createUniqueIdentifierString()));
		items.add(new Item("Onions", "123456", 1, Unit.KG, Identifier.createUniqueIdentifierString()));
		items.add(new Item("TODO name changed", "David", 4, Unit.COUNT, assignment.getItems().get(0).getId()));
		return items;
	}
	
	@Test
	public void updateEventLevelAssignmentItems() throws IllegalDataOperation, DataNotFoundException, DatastoreException, UpdateVersionException, UpdateException{
		GeoPosition geoPosition = new GeoPosition(0,0);
		EventDo event = new EventDo("TestEvent", Calendar.getInstance(), 
				Calendar.getInstance(), false, null, new Location("San Jose", "CA", geoPosition));
		event.setAssignment(new Assignment());
		event.getAssignment().getItems().add(new Item("blankets", "David", 4, Unit.COUNT, Identifier.createUniqueIdentifierString()));
		event.getAssignment().getItems().add(new Item("rice", "Roger", 10, Unit.LB, Identifier.createUniqueIdentifierString()));
		
    	EventDatastoreImpl.storeEventOnly(event);
		List<Item> toBeAdded = new ArrayList<>();
		List<Item> toBeRemoved = new ArrayList<>();
	
		// Add new item
		Item i1 = new Item("Onions", "9876543", 1, Unit.KG, Identifier.createUniqueIdentifierString());
		toBeAdded.add(i1);
		EventDo updatedEvent = AssignmentDatastoreImpl.updateEventAssignmentItems(toBeAdded, null, event.getId(), event.getVersion());
		
		List<Item> expected = ListUtils.union(event.getAssignment().getItems(), toBeAdded);
		TestUtil.isEqualList(updatedEvent.getAssignment().getItems(), expected);
		
		// Add and update and remove
		toBeAdded.clear();
		i1 = new Item("Tent", "Nakul", 1, Unit.KG, Identifier.createUniqueIdentifierString());
		toBeAdded.add(i1);
		// Update existing item
		updatedEvent.getAssignment().getItems().get(0).setCount(10);
		toBeAdded.add(updatedEvent.getAssignment().getItems().get(0));
		// Remove
		toBeRemoved.add(updatedEvent.getAssignment().getItems().get(2));
		
		EventDo updatedEvent1 = AssignmentDatastoreImpl.updateEventAssignmentItems(toBeAdded, toBeRemoved,
				updatedEvent.getId(), updatedEvent.getVersion());
		expected = new ArrayList<Item>();
		
		// Handpicking since "2" was removed
		expected.add(updatedEvent.getAssignment().getItems().get(1));
		expected.addAll(toBeAdded);
		Assert.assertEquals(toBeAdded.get(1).getCount(), updatedEvent1.getAssignment().getItems().get(0).getCount());
		Assert.assertTrue(TestUtil.isEqualList(expected, updatedEvent1.getAssignment().getItems()));	
		
		// Only remove
		toBeRemoved.clear();
		toBeRemoved.add(updatedEvent1.getAssignment().getItems().get(0));
		toBeRemoved.add(updatedEvent1.getAssignment().getItems().get(1));
    	
		EventDo updatedEvent2 = AssignmentDatastoreImpl.updateEventAssignmentItems(null, toBeRemoved,
				updatedEvent1.getId(), updatedEvent1.getVersion());
    	expected.clear();
    	expected.add(updatedEvent1.getAssignment().getItems().get(2));
    	Assert.assertTrue(TestUtil.isEqualList(expected, updatedEvent2.getAssignment().getItems()));		
    }
	
	
}
