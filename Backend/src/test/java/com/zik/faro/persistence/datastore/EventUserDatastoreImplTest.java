package com.zik.faro.persistence.datastore;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.data.Location;
import com.zik.faro.data.GeoPosition;
import com.zik.faro.data.expense.ExpenseGroup;
import com.zik.faro.data.user.Address;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.EventUserDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;
import org.junit.*;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class EventUserDatastoreImplTest {
    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
        ObjectifyService.register(EventUserDo.class);
        ObjectifyService.register(EventDo.class);
        ObjectifyService.register(FaroUserDo.class);
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
    public void testEventUserLoadStore(){
        final String eventName = UUID.randomUUID().toString();
		GeoPosition geoPosition = new GeoPosition(0,0);
        EventDo testEvent = new EventDo(eventName,
        		Calendar.getInstance(),
        		Calendar.getInstance(),
                false,
                new ExpenseGroup("Lake Shasta", "shasta123"),
                new Location("Lake Shasta", "Lake Shasta's Address", geoPosition));
        DatastoreObjectifyDAL.storeObject(testEvent);


        FaroUserDo faroUser = new FaroUserDo("user@gmail.com", "SampleUser", null, "Gilmour", "dg@splitwise.com",
                "2323", new Address(123, "Palm Avenue", "Stanford", "CA", 94332));
        DatastoreObjectifyDAL.storeObject(faroUser);

        EventUserDatastoreImpl.storeEventUser(testEvent.getId(), faroUser.getId());
        EventUserDo eventUser = EventUserDatastoreImpl.loadEventUser(testEvent.getId(), faroUser.getId());
        Assert.assertNotNull(eventUser);

        EventDo retEvent = eventUser.getEvent();
        Assert.assertEquals(testEvent.getEventName(), retEvent.getEventName());

        FaroUserDo retFaroUser = eventUser.getFaroUser();
        Assert.assertEquals(faroUser.getFirstName(), retFaroUser.getFirstName());
    }

    @Test
    public void testEventUserLoadDelete(){
		GeoPosition geoPosition1 = new GeoPosition(0,0);
        EventDo event1 = new EventDo("Event1",
        		Calendar.getInstance(),
        		Calendar.getInstance(),
                false,
                new ExpenseGroup("ExpenseGroupName1", "ExpenseGroupId1"),
                new Location("Location1", "Address1", geoPosition1));
        DatastoreObjectifyDAL.storeObject(event1);

		GeoPosition geoPosition2 = new GeoPosition(100,100);
        EventDo event2 = new EventDo("Event2",
        		Calendar.getInstance(),
        		Calendar.getInstance(),
                false,
                new ExpenseGroup("ExpenseGroupName2", "ExpenseGroupId2"),
                new Location("Location2", "Address2", geoPosition2));
        DatastoreObjectifyDAL.storeObject(event2);

		GeoPosition geoPosition3 = new GeoPosition(50,50);
        EventDo event3 = new EventDo("Event3",
        		Calendar.getInstance(),
        		Calendar.getInstance(),
                false,
                new ExpenseGroup("ExpenseGroupName3", "ExpenseGroupId3"),
                new Location("Location3", "Address3", geoPosition3));
        DatastoreObjectifyDAL.storeObject(event3);

        FaroUserDo faroUser1 = new FaroUserDo("user1@gmail.com", "FirstNAme1", null, "LastName1", "expenseid1@splitwise.com",
                "0000001", new Address(1, "Palm Avenue1", "Stanford1", "CA", 94332));
        DatastoreObjectifyDAL.storeObject(faroUser1);

        FaroUserDo faroUser2 = new FaroUserDo("user2@gmail.com", "FirstNAme2", null, "LastName2", "expenseid2@splitwise.com",
                "0000002", new Address(2, "Palm Avenue2", "Stanford2", "CA", 94332));
        DatastoreObjectifyDAL.storeObject(faroUser2);

        EventUserDatastoreImpl.storeEventUser(event1.getId(), faroUser1.getId());
        EventUserDatastoreImpl.storeEventUser(event1.getId(), faroUser2.getId());
        EventUserDatastoreImpl.storeEventUser(event2.getId(), faroUser1.getId());
        EventUserDatastoreImpl.storeEventUser(event3.getId(), faroUser1.getId());


        // load test
        List<EventUserDo> userList1 = EventUserDatastoreImpl.loadEventUserByEvent(event1.getId());
        Assert.assertEquals(2, userList1.size());
        List<EventUserDo> userList2 = EventUserDatastoreImpl.loadEventUserByEvent(event2.getId());
        Assert.assertEquals(1, userList2.size());
        List<EventUserDo> eventList = EventUserDatastoreImpl.loadEventUserByFaroUser(faroUser1.getId());
        Assert.assertEquals(3, eventList.size());
        List<EventUserDo> eventList2 = EventUserDatastoreImpl.loadEventUserByFaroUser(faroUser2.getId());
        Assert.assertEquals(1, eventList2.size());

        // delete test
        EventUserDatastoreImpl.deleteEventUserByEvent(event1.getId());
        userList1 = EventUserDatastoreImpl.loadEventUserByEvent(event1.getId());
        Assert.assertEquals(0, userList1.size());
        EventUserDatastoreImpl.deleteEventUserByEvent(event2.getId());
        userList2 = EventUserDatastoreImpl.loadEventUserByEvent(event2.getId());
        Assert.assertEquals(0, userList2.size());
    }

    @Test
    public void testEventUserIdempotency(){
		GeoPosition geoPosition1 = new GeoPosition(0,0);
        EventDo event1 = new EventDo("Event1",
        		Calendar.getInstance(),
        		Calendar.getInstance(),
                false,
                new ExpenseGroup("ExpenseGroupName1", "ExpenseGroupId1"),
                new Location("Location1", "Address1", geoPosition1));
        DatastoreObjectifyDAL.storeObject(event1);

        FaroUserDo faroUser1 = new FaroUserDo("user1@gmail.com", "FirstNAme1", null, "LastName1", "expenseid1@splitwise.com",
                "0000001", new Address(1, "Palm Avenue1", "Stanford1", "CA", 94332));
        DatastoreObjectifyDAL.storeObject(faroUser1);

        /*Repeat 'n' times */
        EventUserDatastoreImpl.storeEventUser(event1.getId(), faroUser1.getId());
        EventUserDatastoreImpl.storeEventUser(event1.getId(), faroUser1.getId());
        EventUserDatastoreImpl.storeEventUser(event1.getId(), faroUser1.getId());

        List<EventUserDo> userList1 = EventUserDatastoreImpl.loadEventUserByEvent(event1.getId());
        Assert.assertEquals(1, userList1.size());
    }
    
    @Test
    public void testEventUserRemoval(){
    	// Create Event
		GeoPosition geoPosition1 = new GeoPosition(0,0);
    	EventDo event1 = new EventDo("Event1",
    			Calendar.getInstance(),
    			Calendar.getInstance(),
                false,
                new ExpenseGroup("ExpenseGroupName1", "ExpenseGroupId1"),
                new Location("Location1", "Address1", geoPosition1));
        DatastoreObjectifyDAL.storeObject(event1);
        // Create FaroUser
        FaroUserDo faroUser1 = new FaroUserDo("user1@gmail.com", "FirstNAme1", null, "LastName1", "expenseid1@splitwise.com",
                "0000001", new Address(1, "Palm Avenue1", "Stanford1", "CA", 94332));
        DatastoreObjectifyDAL.storeObject(faroUser1);
        
        // Establish user-event connectivity. User invited to event
        EventUserDatastoreImpl.storeEventUser(event1.getId(), faroUser1.getId());
        
        // Verify user-event relation
        EventUserDo eventUser = EventUserDatastoreImpl.loadEventUser(event1.getId(),
        		faroUser1.getId());
        Assert.assertNotNull(eventUser);
        Assert.assertEquals(event1,eventUser.getEvent());
        Assert.assertEquals(faroUser1, eventUser.getFaroUser());
        
        // Delete user from event
        EventUserDatastoreImpl.deleteEventUser(event1.getId(), faroUser1.getId());
        
        // Verify deletion
        eventUser = EventUserDatastoreImpl.loadEventUser(event1.getId(),
        		faroUser1.getId());
        Assert.assertNull(eventUser);
    }
}
