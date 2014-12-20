package com.zik.faro.persistence.datastore;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import org.junit.*;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class DatastoreObjectifyDALTest {

    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
        ObjectifyService.register(TestClass.class);
        ObjectifyService.register(A.class);
        ObjectifyService.register(B.class);
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
        TestClass retrievedTestClass = DatastoreObjectifyDAL.loadFirstObjectByIndexedStringFieldEQ("firstName", "sammy", TestClass.class);
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
                DatastoreObjectifyDAL.loadObjectsByIndexedStringFieldEQ("firstName", "sammy", TestClass.class);
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
                DatastoreObjectifyDAL.loadObjectsByStringFilters(keyFilter1, new HashMap<String, String>(), TestClass.class);
        Assert.assertEquals(1, retrievedObjects.size());

        /*Create additional filter on indexed fields*/
        Map<String, String> filterForTestIndexField = new HashMap<>();
        filterForTestIndexField.put("firstName", testIndexField);
        retrievedObjects = DatastoreObjectifyDAL.loadObjectsByStringFilters(keyFilter1, filterForTestIndexField, TestClass.class);
        Assert.assertEquals(1, retrievedObjects.size());


        /*Create a filter with a index field value which is not present in conjunction with the key filter*/
        Map<String, String> wrongFilter = new HashMap<>();
        wrongFilter.put("firstName", "name2");
        retrievedObjects = DatastoreObjectifyDAL.loadObjectsByStringFilters(keyFilter1, wrongFilter, TestClass.class);
        Assert.assertEquals(0, retrievedObjects.size());

        /*Create and store and object with a duplicate value for the indexed field*/
        TestClass object3 = new TestClass("object3", testIndexField);
        DatastoreObjectifyDAL.storeObject(object3);

        /*Retrieved only by index field and verify that 2 instances are now returned*/
        retrievedObjects = DatastoreObjectifyDAL.loadObjectsByStringFilters(new HashMap<DatastoreOperator, String>(),
                filterForTestIndexField,
                TestClass.class);
        Assert.assertEquals(2, retrievedObjects.size());
    }


    @Entity
    class A{
        @Id
        public String id;
        public String name;
        A(String id, String name){
            this.id = id; this.name = name;
        }
    }

    @Entity
    class B{
        @Id
        public String id;
        @Parent @Index
        public Ref<A> aRef;
        public int val;
        B(String id, String aRef, int val){
            this.id = id; this.val = val;
            this.aRef = Ref.create(Key.create(A.class, aRef));
        }
    }

    @Test
    public void testParentRelation(){
        /* A is parent of B */

        A a1 = new A("a1", "a1");
        DatastoreObjectifyDAL.storeObject(a1);

        A a2 = new A("a2", "a2");
        DatastoreObjectifyDAL.storeObject(a2);

        B b1 = new B("b1", "a1", 4);
        DatastoreObjectifyDAL.storeObject(b1);
        //Load parent
        A ra1 = ofy().load().type(A.class).id("a1").now();
        Assert.assertNotNull(ra1);

        // Load entity using ParentId
        B rb1 = DatastoreObjectifyDAL.loadObjectWithParentId(A.class, "a1", B.class, "b1");
        Assert.assertNotNull(rb1);
        Assert.assertEquals(4, rb1.val);

        // Create B entity with same parentId and different Id
        B b2 = new B("b2", "a1", 5);
        DatastoreObjectifyDAL.storeObject(b2);
        B rb2 = DatastoreObjectifyDAL.loadObjectWithParentId(A.class, "a1", B.class, "b2");
        Assert.assertNotNull(rb2);
        Assert.assertEquals(b2.val, rb2.val);

        /* *** LINES BELOW THIS INDICATE THAT ENTITY WITH THE PARENT, IS IDENTIFIED BY PARENT + ID, NOT JUST ID
         * ENTITY WITH ID B1 AND PARENT A1 IS DIFFERENT FROM ENTITY WITH ID B1 AND PARENT A2
         */

        // Create A entity with different parentId and same Id
        B b3 = new B("b1", "a2", 6);
        DatastoreObjectifyDAL.storeObject(b3);
        // Verify the latest b3 objects stored with id b1, parent a2
        B rb3 = DatastoreObjectifyDAL.loadObjectWithParentId(A.class, "a2", B.class, "b1");
        Assert.assertEquals(b3.val, rb3.val);

        // Verify the latest b1 object stored with id b1, parent a1
        B rbTemp = DatastoreObjectifyDAL.loadObjectWithParentId(A.class, "a1", B.class, "b1");
        Assert.assertEquals(b1.val, rbTemp.val);

        List<B> childrenOfa1 = DatastoreObjectifyDAL.loadObjectsByAncestorRef(A.class, "a1", B.class);
        Assert.assertEquals(2, childrenOfa1.size());
    }



}
