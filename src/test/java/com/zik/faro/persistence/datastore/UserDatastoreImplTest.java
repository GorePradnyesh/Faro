package com.zik.faro.persistence.datastore;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import org.junit.*;

import java.util.List;


public class UserDatastoreImplTest {

    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
        ObjectifyService.register(FaroUser.class);
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
    public void testLoadUser(){
        final String email = "dg@gmail.com";
        final String firstName = "David";
        FaroUser faroUser = new FaroUser(email, firstName, null, "Gilmour", "dg@splitwise.com",
                "2323", new Address(123, "Palm Avenue", "Stanford", "CA", 94332));
        UserDatastoreImpl.storeUser(faroUser);

        /*Test Load User by ID*/
        FaroUser retrievedUser = UserDatastoreImpl.loadFaroUserById(email);
        Assert.assertNotNull(retrievedUser);

        /*Test load users by index field*/
        List<FaroUser> userList = UserDatastoreImpl.loadFaroUsersByName(firstName);
        Assert.assertEquals(1, userList.size());

        /*Store another user with the same fist name*/
        FaroUser faroUser2 = new FaroUser("dguetta@gmail.com", firstName, null, "Gilmour", "dguetta@splitwise.com",
                "1323", new Address(456, "Venice Beach", "Los Angeles", "CA", 93411));
        UserDatastoreImpl.storeUser(faroUser2);


        /*Load the users by first name and make sure that the list now includes the new user*/
        userList = UserDatastoreImpl.loadFaroUsersByName(firstName);
        Assert.assertEquals(2, userList.size());
    }
}
