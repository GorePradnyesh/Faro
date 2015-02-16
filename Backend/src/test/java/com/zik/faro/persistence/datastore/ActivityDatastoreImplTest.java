package com.zik.faro.persistence.datastore;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.data.*;

import org.junit.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ActivityDatastoreImplTest {

    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
        ObjectifyService.register(Activity.class);
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
    public void loadActivityByIdTest(){
        final String testEventId = "Event1";

        /*Create and store test activity 1*/
        Activity activity1 = new Activity(testEventId, "dummyname", "dummyDescription",
                new Location("Lake Shasta"),
                new DateOffset(new Date(), 60 * 1000));
        Assignment tempAssignment = new Assignment();
        tempAssignment.addItem(new Item("blankets", "David", 4, Unit.COUNT));
        tempAssignment.addItem(new Item("rice", "Roger", 10, Unit.LB));
        activity1.setAssignment(tempAssignment);
        ActivityDatastoreImpl.storeActivity(activity1);

        Activity retrievedActivity = ActivityDatastoreImpl.loadActivityById(activity1.getId(), testEventId);
        Assert.assertEquals(activity1.getId(), retrievedActivity.getId());
        Assert.assertEquals(activity1.getAssignment().id, retrievedActivity.getAssignment().id);

        /*Create and store new Activity for the same event ( but of course with different ID) */
        Activity activity2 = new Activity(testEventId, "dummyname2", "dummyDescription2",
                new Location("Lake Shasta2"),
                new DateOffset(new Date(), 75 * 1000));
        Assignment tempAssignment2 = new Assignment();
        tempAssignment2.addItem(new Item("Rope", "David", 12, Unit.METER));
        tempAssignment2.addItem(new Item("Poop", "Roger", 3, Unit.LB));
        activity2.setAssignment(tempAssignment2);
        ActivityDatastoreImpl.storeActivity(activity2);

        /*Load the activities for testEventID and verify that both activities are retrieved*/
        List<Activity> activityList = ActivityDatastoreImpl.loadActivitiesByEventId(testEventId);
        Assert.assertEquals(2, activityList.size());
    }
}
