package com.zik.faro.persistence.datastore;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import org.junit.*;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.UUID;

public class DatastoreObjectifyDALTest {

    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
        ObjectifyService.register(FaroUser.class);
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
        FaroUser faroUser = new FaroUser("dg@gmail.com", "David", null, "Gilmour", "dg@splitwise.com",
                "2323", new Address(123, "Palm Avenue", "Stanford", "CA", 94332));
        DatastoreObjectifyDAL.storeObject(faroUser);
        FaroUser user = DatastoreObjectifyDAL.loadObjectById("dg@gmail.com", FaroUser.class);
        Assert.assertNotNull(user);
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

}
