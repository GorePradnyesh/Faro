package com.zik.faro.api.unit;

import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.sun.jersey.api.JResponse;
import com.zik.faro.api.authentication.LoginHandler;
import com.zik.faro.api.authentication.SignupHandler;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.UserCredentials;

/**
 * Created by granganathan on 3/30/15.
 */
public class LoginApiTest {
    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static {
        ObjectifyService.register(FaroUser.class);
        ObjectifyService.register(UserCredentials.class);
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
    public void testLoginApi() {
        final String fName = UUID.randomUUID().toString();
        FaroUser faroUser = new FaroUser("rwaters@gmail.com",
                fName, null, "waters",
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        createNewUser(faroUser, "pfloyd782$");

        LoginHandler loginHandler = new LoginHandler();
        JResponse<String> token = loginHandler.login(faroUser.getEmail(), "pfloyd782$");

        Assert.assertNotNull(token.getEntity());
    }

    public String createNewUser(FaroUser user, String password) {
        SignupHandler signupHandler = new SignupHandler();
        return signupHandler.signupUser(new FaroSignupDetails(user, password)).getEntity();
    }
}
