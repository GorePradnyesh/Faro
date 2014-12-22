package com.zik.faro.api.unit;


import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.api.profile.ProfileHandler;
import com.zik.faro.data.Event;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import org.junit.*;

import java.util.UUID;

public class ProfileApiTest {
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
    public void testCreateUser(){
        final String fName = UUID.randomUUID().toString();
        FaroUser user = new FaroUser("rwaters@gmail.com",
                fName, null, "waters",
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        ProfileHandler profileHandler = new ProfileHandler();
        profileHandler.createProfile("signatureWithUserName", user);

        FaroUser retrievedUser = profileHandler.getProfile(user.getId());
        Assert.assertEquals(user.getFirstName(), retrievedUser.getFirstName());
    }
}
