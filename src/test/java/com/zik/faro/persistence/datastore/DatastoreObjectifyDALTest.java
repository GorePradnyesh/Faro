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

import java.util.*;

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


    @Test
    public void testMultiGet(){
        List<String> keys = new ArrayList<>();

        A a1 = new A("a1", "a1");
        DatastoreObjectifyDAL.storeObject(a1);
        keys.add(a1.id);

        A a2 = new A("a2", "a2");
        DatastoreObjectifyDAL.storeObject(a2);
        keys.add(a2.id);

        A a3 = new A("a3", "a3");
        DatastoreObjectifyDAL.storeObject(a3);
        keys.add(a3.id);

        Map<String, A> objects = DatastoreObjectifyDAL.loadMultipleObjectsByIdSync(keys, A.class);
        Assert.assertEquals(3, objects.size());

        List<String> badKeys = new ArrayList<>();
        badKeys.add("badKey1");
        badKeys.add("badKey2");
        objects = DatastoreObjectifyDAL.loadMultipleObjectsByIdSync(badKeys, A.class);
        Assert.assertTrue(objects.isEmpty());
    }

    /** 
     * Test includes both single and bulk variants of the deleteObjectById api
     */
    @Test
    public void testDeleteObjectById(){
    	// Create data for testing
    	A a1 = new A("a10", "a10");DatastoreObjectifyDAL.storeObject(a1);
    	A a2 = new A("a11", "a11");DatastoreObjectifyDAL.storeObject(a2);
    	A a3 = new A("a12", "a12");DatastoreObjectifyDAL.storeObject(a3);
    	
    	// Verify data indeed created
        A retrievedObj = DatastoreObjectifyDAL.loadObjectById(a1.id, A.class);
        Assert.assertEquals(a1.id, retrievedObj.id);
        Assert.assertEquals(a1.name, retrievedObj.name);
        retrievedObj = DatastoreObjectifyDAL.loadObjectById(a2.id, A.class);
        Assert.assertEquals(a2.id, retrievedObj.id);
        Assert.assertEquals(a2.name, retrievedObj.name);
        retrievedObj = DatastoreObjectifyDAL.loadObjectById(a3.id, A.class);
        Assert.assertEquals(a3.id, retrievedObj.id);
        Assert.assertEquals(a3.name, retrievedObj.name);
        
        // Delete
        DatastoreObjectifyDAL.delelteObjectById(a1.id, A.class);
        retrievedObj = DatastoreObjectifyDAL.loadObjectById(a1.id, A.class);
        Assert.assertNull(retrievedObj);
        
        List<String> list = new ArrayList<String>();
        list.add(a2.id);list.add(a3.id);
        
        // Delete (Bulk)
        DatastoreObjectifyDAL.delelteObjectsById(list, A.class);
    }
    
    /** 
     * Test includes both single and bulk variants of the deleteObjectByIdWithParentId api
     */
    @Test
    public void testDeleteObjectByIdWithParentId(){
    	// Create data for testing
    	A parent = new A("parent", "parent");DatastoreObjectifyDAL.storeObject(parent);        
        B child = new B("child", "parent", 44);DatastoreObjectifyDAL.storeObject(child);
        B child1 = new B("child1", "parent", 45);DatastoreObjectifyDAL.storeObject(child1);
        B child2 = new B("child2", "parent", 46);DatastoreObjectifyDAL.storeObject(child2);
        
        // Verify data indeed created
        A retrievedParent = DatastoreObjectifyDAL.loadObjectById(parent.id, A.class);
        B retrievedChild = DatastoreObjectifyDAL.loadObjectWithParentId(A.class, parent.id, B.class, child.id);
        B retrievedChild1 = DatastoreObjectifyDAL.loadObjectWithParentId(A.class, parent.id, B.class, child1.id);
        B retrievedChild2 = DatastoreObjectifyDAL.loadObjectWithParentId(A.class, parent.id, B.class, child2.id);
        Assert.assertEquals(parent.id, retrievedParent.id);
        Assert.assertEquals(child.id, retrievedChild.id);
        Assert.assertEquals(child1.id, retrievedChild1.id);Assert.assertEquals(child2.id, retrievedChild2.id);
        
        // Delete child
        DatastoreObjectifyDAL.deleteObjectByIdWithParentId(child.id, B.class, "parent", A.class);
        
        // Verify indeed deleted
        retrievedChild = DatastoreObjectifyDAL.loadObjectWithParentId(A.class, parent.id, B.class, child.id);
        Assert.assertNull(retrievedChild);
        
        List<String> children = new ArrayList<String>();
        children.add(child1.id);children.add(child2.id);
        
        // Delete child1 and child2 (Bulk call)
        DatastoreObjectifyDAL.deleteObjectsByIdWithParentId(children, B.class, parent.id, A.class);
        // Cannot verify since deletion is async.
    }
    
    /**
     * Test includes both single and bulk variants of deleteEntity
     */
    @Test
    public void testDeleteEntity(){
    	A a1 = new A("a10", "a10");DatastoreObjectifyDAL.storeObject(a1);
    	A a2 = new A("a11", "a11");DatastoreObjectifyDAL.storeObject(a2);
    	A a3 = new A("a12", "a12");DatastoreObjectifyDAL.storeObject(a3);
    	
    	// Verify data indeed created
        A retrievedObj = DatastoreObjectifyDAL.loadObjectById(a1.id, A.class);
        Assert.assertEquals(a1.id, retrievedObj.id);
        Assert.assertEquals(a1.name, retrievedObj.name);
        retrievedObj = DatastoreObjectifyDAL.loadObjectById(a2.id, A.class);
        Assert.assertEquals(a2.id, retrievedObj.id);
        Assert.assertEquals(a2.name, retrievedObj.name);
        retrievedObj = DatastoreObjectifyDAL.loadObjectById(a3.id, A.class);
        Assert.assertEquals(a3.id, retrievedObj.id);
        Assert.assertEquals(a3.name, retrievedObj.name);
        
        // Delete
        DatastoreObjectifyDAL.deleteEntity(a1);
        
        // Verify indeed deleted
        retrievedObj = DatastoreObjectifyDAL.loadObjectById(a1.id, A.class);
        Assert.assertNull(retrievedObj);
        
        List<A> list = new ArrayList<A>(); list.add(a2); list.add(a3);
        // Delete (Bulk)
        DatastoreObjectifyDAL.deleteEntities(list);
        // Cannot verify since deletion is async.
    }

}
