package com.zik.faro.persistence.datastore;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.GeoPosition;
import com.zik.faro.data.Location;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.expense.ExpenseGroup;
import com.zik.faro.persistence.datastore.data.EventDo;

public class EventDatastoreImplTest {

    private static final LocalServiceTestHelper helper =
    		new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
            .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));
    

    static{
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

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void testSimpleStoreLoad() throws DataNotFoundException{
        String eventNameSuffix = UUID.randomUUID().toString();
		GeoPosition geoPosition = new GeoPosition(0,0);
        EventDo testEvent = new EventDo("Lake Shasta "+ eventNameSuffix,
        		Calendar.getInstance(),
        		Calendar.getInstance(),
                false,
                new ExpenseGroup("Lake Shasta", "shasta123"),
                new Location("Home", "667 Encore Way", geoPosition));

        EventDatastoreImpl.storeEventOnly(testEvent);
        EventDo loadedEvent = EventDatastoreImpl.loadEventByID(testEvent.getId());
        Assert.assertNotNull(loadedEvent);
        Assert.assertEquals(loadedEvent.getEventName(), testEvent.getEventName());
        Assert.assertEquals(loadedEvent.getId(), testEvent.getId());
        Assert.assertEquals(loadedEvent.getAssignment(), testEvent.getAssignment());
        Assert.assertEquals(loadedEvent.getEndDate(), testEvent.getEndDate());
        Assert.assertEquals(loadedEvent.getStartDate(), testEvent.getStartDate());
        Assert.assertEquals(loadedEvent.getStatus(), testEvent.getStatus());
        Assert.assertEquals(loadedEvent.getLocation().getLocationName(), testEvent.getLocation().getLocationName());
        Assert.assertEquals(loadedEvent.getLocation().getLocationAddress(), testEvent.getLocation().getLocationAddress());
        Assert.assertEquals(loadedEvent.getLocation().getPosition(), testEvent.getLocation().getPosition());
        Assert.assertEquals(loadedEvent.getExpenseGroup().groupId, testEvent.getExpenseGroup().groupId);
        Assert.assertEquals(loadedEvent.getExpenseGroup().groupName, testEvent.getExpenseGroup().groupName);
        Assert.assertEquals(loadedEvent.isControlFlag(), testEvent.isControlFlag());
        
    }
    
    @Test
    public void testUpdateEvent() throws DataNotFoundException, DatastoreException, UpdateVersionException, UpdateException{
		GeoPosition geoPosition1 = new GeoPosition(0,0);
    	EventDo testEvent = new EventDo("Lake Shasta "+ UUID.randomUUID().toString(),
        		Calendar.getInstance(),
        		Calendar.getInstance(),
                false,
                new ExpenseGroup("Lake Shasta", "shasta123"),
                new Location("Home", "667 Encore Way", geoPosition1));
    	EventDatastoreImpl.storeEventOnly(testEvent);
    	Set<String> updatedFields = new HashSet<String>();
        // Update Event
    	EventDo updateObj = new EventDo();
		GeoPosition geoPosition2 = new GeoPosition(100,100);
		
    	updateObj.setId(testEvent.getId());
    	updateObj.setVersion(testEvent.getVersion());
    	updateObj.setEndDate(Calendar.getInstance());
    	updatedFields.add("endDate");
        updateObj.setEventDescription("Updated description");
        updatedFields.add("eventDescription");
        updateObj.setEventName("Updated event name");
        updatedFields.add("eventName");
        updateObj.setLocation(new Location("Crater Lake", "Oregon", geoPosition2));
        updatedFields.add("location");
        updateObj.setStartDate(Calendar.getInstance());
        updatedFields.add("startDate");
        updateObj.setStatus(ObjectStatus.CLOSED);
        updatedFields.add("status");
        
        // Call update API
        EventDatastoreImpl.updateEvent(updateObj.getId(), updateObj, updatedFields);
        
        // Verify. 
        EventDo returnedEventDo = EventDatastoreImpl.loadEventByID(updateObj.getId());
        Assert.assertEquals(updateObj.getEventDescription(), returnedEventDo.getEventDescription());
        Assert.assertEquals(updateObj.getEventName(), returnedEventDo.getEventName());
        Assert.assertEquals(updateObj.getEndDate().getTimeInMillis(), returnedEventDo.getEndDate().getTimeInMillis());
        Assert.assertEquals(updateObj.getStartDate().getTimeInMillis(), returnedEventDo.getStartDate().getTimeInMillis());
        Assert.assertEquals(updateObj.getStatus(), returnedEventDo.getStatus());
        Assert.assertEquals(updateObj.getLocation().getLocationName(), returnedEventDo.getLocation().getLocationName());
        Assert.assertEquals(updateObj.getLocation().getLocationAddress(), returnedEventDo.getLocation().getLocationAddress());
        Assert.assertEquals(updateObj.getLocation().getPosition(), returnedEventDo.getLocation().getPosition());
        Long newVersion = updateObj.getVersion();
        Assert.assertEquals(++newVersion, returnedEventDo.getVersion());
        
        // Update with older version and verify failure
        try{
        	EventDatastoreImpl.updateEvent(updateObj.getId(), updateObj, updatedFields);
    	} catch(UpdateVersionException e){
    		Assert.assertNotNull(e);
    		Assert.assertEquals(e.getMessage(), "Incorrect entity version. Current version:2");
    		return;
    	}
        
    }
}
