package com.zik.faro.persistence.datastore;

import java.util.Calendar;
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
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.PollDo;
import com.zik.faro.data.Location;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.PollOption;
import com.zik.faro.data.expense.ExpenseGroup;

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
        EventDo testEvent = new EventDo("Lake Shasta "+ eventNameSuffix,
        		Calendar.getInstance(),
        		Calendar.getInstance(),
                false,
                new ExpenseGroup("Lake Shasta", "shasta123"),
                new Location("Lake Shasta"));

        EventDatastoreImpl.storeEventOnly(testEvent);
        EventDo loadedEvent = EventDatastoreImpl.loadEventByID(testEvent.getEventId());
        Assert.assertNotNull(loadedEvent);
        Assert.assertEquals(loadedEvent.getEventName(), testEvent.getEventName());
        Assert.assertEquals(loadedEvent.getEventId(), testEvent.getEventId());
        Assert.assertEquals(loadedEvent.getAssignment(), testEvent.getAssignment());
        Assert.assertEquals(loadedEvent.getEndDate(), testEvent.getEndDate());
        Assert.assertEquals(loadedEvent.getStartDate(), testEvent.getStartDate());
        Assert.assertEquals(loadedEvent.getStatus(), testEvent.getStatus());
        Assert.assertEquals(loadedEvent.getLocation().locationName, testEvent.getLocation().locationName);
        Assert.assertEquals(loadedEvent.getExpenseGroup().groupId, testEvent.getExpenseGroup().groupId);
        Assert.assertEquals(loadedEvent.getExpenseGroup().groupName, testEvent.getExpenseGroup().groupName);
        Assert.assertEquals(loadedEvent.isControlFlag(), testEvent.isControlFlag());
        
    }
    
    @Test
    public void testUpdateEvent() throws DataNotFoundException, DatastoreException{
    	EventDo testEvent = new EventDo("Lake Shasta "+ UUID.randomUUID().toString(),
        		Calendar.getInstance(),
        		Calendar.getInstance(),
                false,
                new ExpenseGroup("Lake Shasta", "shasta123"),
                new Location("Lake Shasta"));
    	EventDatastoreImpl.storeEventOnly(testEvent);
        // Update Event
    	EventDo updateObj = new EventDo();
    	updateObj.setEventId(testEvent.getEventId());
    	
    	updateObj.setEndDate(Calendar.getInstance());
        updateObj.setEventDescription("Updated description");
        updateObj.setEventName("Updated event name");
        updateObj.setLocation(new Location("Crater Lake"));
        updateObj.setStartDate(Calendar.getInstance());
        updateObj.setStatus(ObjectStatus.CLOSED);
        
        // Call update API
        EventDatastoreImpl.updateEvent(updateObj.getEventId(), updateObj);
        
        // Verify. 
        EventDo returnedEventDo = EventDatastoreImpl.loadEventByID(updateObj.getEventId());
        Assert.assertEquals(updateObj.getEventDescription(), returnedEventDo.getEventDescription());
        Assert.assertEquals(updateObj.getEventName(), returnedEventDo.getEventName());
        Assert.assertEquals(updateObj.getEndDate(), returnedEventDo.getEndDate());
        Assert.assertEquals(updateObj.getStartDate(), returnedEventDo.getStartDate());
        Assert.assertEquals(updateObj.getStatus(), returnedEventDo.getStatus());
        Assert.assertEquals(updateObj.getLocation().locationName, returnedEventDo.getLocation().locationName);
    }
}
