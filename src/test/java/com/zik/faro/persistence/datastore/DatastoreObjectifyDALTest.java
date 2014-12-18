package com.zik.faro.persistence.datastore;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import org.junit.*;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatastoreObjectifyDALTest {

    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
        ObjectifyService.register(TestClass.class);
    }

    @XmlRootElement
    @Entity
    class TestClass {
        @Id @Index
        public String id;
        @Index
        public String firstName;

        TestClass(String name){
            this.firstName = name;
        }

        TestClass(String id, String name){
            this.id = id;
            this.firstName = name;
        }
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
    public void testLoadObjectById() {
        final String testID = UUID.randomUUID().toString();
        TestClass testObject = new TestClass(testID, "indexedField1");
        DatastoreObjectifyDAL.storeObject(testObject);
        TestClass obj = DatastoreObjectifyDAL.loadObjectById(testID, TestClass.class);
        Assert.assertNotNull(obj);
        Assert.assertEquals(testID, obj.id);
    }

    @Test
    public void testLoadFirstObjectByIndexedFieldEQ(){
        TestClass testClass = new TestClass("sammy");
        testClass.id = "possible1";
        DatastoreObjectifyDAL.storeObject(testClass);
        TestClass retrievedTestClass = DatastoreObjectifyDAL.loadFirstObjectByIndexedFieldEQ("firstName", "sammy", TestClass.class);
        Assert.assertEquals(testClass.firstName, retrievedTestClass.firstName);
    }


    @Test
    public void testLoadObjectsByIndexField(){
        TestClass firstObject = new TestClass("sammy");
        firstObject.id = UUID.randomUUID().toString();
        DatastoreObjectifyDAL.storeObject(firstObject);

        TestClass secondObject = new TestClass("sammy");
        secondObject.id = UUID.randomUUID().toString();
        DatastoreObjectifyDAL.storeObject(secondObject);

        List<TestClass> objectList =
                DatastoreObjectifyDAL.loadObjectsByIndexedFieldEQ("firstName", "sammy", TestClass.class);
        Assert.assertEquals(2, objectList.size());
    }

    @Test
    public void testLoadWithFilterChain(){
        final String testId = "object1";
        final String testIndexField = "name1s";
        TestClass object1 = new TestClass(testId, testIndexField);
        TestClass object2 = new TestClass("objectId2", "name2");

        DatastoreObjectifyDAL.storeObject(object1);
        DatastoreObjectifyDAL.storeObject(object2);

        /*Create Key filter*/
        Map<DatastoreOperator, String> keyFilter1 = new HashMap<>();
        keyFilter1.put(DatastoreOperator.EQ, testId);

        List<TestClass> retrievedObjects =
                DatastoreObjectifyDAL.loadObjectsByFilters(keyFilter1, new HashMap<String, String>(), TestClass.class);
        Assert.assertEquals(1, retrievedObjects.size());

        /*Create additional filter on indexed fields*/
        Map<String, String> filterForTestIndexField = new HashMap<>();
        filterForTestIndexField.put("firstName", testIndexField);
        retrievedObjects = DatastoreObjectifyDAL.loadObjectsByFilters(keyFilter1, filterForTestIndexField, TestClass.class);
        Assert.assertEquals(1, retrievedObjects.size());


        /*Create a filter with a index field value which is not present in conjunction with the key filter*/
        Map<String, String> wrongFilter = new HashMap<>();
        wrongFilter.put("firstName", "name2");
        retrievedObjects = DatastoreObjectifyDAL.loadObjectsByFilters(keyFilter1, wrongFilter, TestClass.class);
        Assert.assertEquals(0, retrievedObjects.size());

        /*Create and store and object with a duplicate value for the indexed field*/
        TestClass object3 = new TestClass("object3", testIndexField);
        DatastoreObjectifyDAL.storeObject(object3);

        /*Retrieved only by index field and verify that 2 instances are now returned*/
        retrievedObjects = DatastoreObjectifyDAL.loadObjectsByFilters(new HashMap<DatastoreOperator, String>(),
                        filterForTestIndexField,
                        TestClass.class);
        Assert.assertEquals(2, retrievedObjects.size());
    }

}
