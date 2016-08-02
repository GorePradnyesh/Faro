package com.zik.faro.api.unit;


import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.TestHelper;
import com.zik.faro.api.profile.ProfileHandler;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

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
        // Setup mock Security context for the handler
        Whitebox.setInternalState(profileHandler, TestHelper.setupMockSecurityContext("rwaters@gmail.com"));
        profileHandler.createProfile(user);

        FaroUser retrievedUser = profileHandler.getProfile().getEntity();
        Assert.assertEquals(user.getFirstName(), retrievedUser.getFirstName());
    }
}
