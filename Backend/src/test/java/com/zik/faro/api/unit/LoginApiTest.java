package com.zik.faro.api.unit;

import java.util.UUID;

import com.zik.faro.applogic.ConversionUtils;
import com.zik.faro.data.user.FaroUser;
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
import com.zik.faro.data.user.FaroSignupDetails;
import com.zik.faro.data.user.Address;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;
import com.zik.faro.persistence.datastore.data.user.UserCredentialsDo;

/**
 * Created by granganathan on 3/30/15.
 */
public class LoginApiTest {
    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static {
        ObjectifyService.register(FaroUserDo.class);
        ObjectifyService.register(UserCredentialsDo.class);
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
    public void toStringTest() {
        final String fName = UUID.randomUUID().toString();
        FaroUserDo faroUser = new FaroUserDo("rwaters@gmail.com",
                fName, null, "waters",
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));

        System.out.println("User = " + faroUser);
    }

    @Test
    public void testLoginApi() {
        final String fName = UUID.randomUUID().toString();
        FaroUserDo faroUser = new FaroUserDo("rwaters@gmail.com",
                fName, null, "waters",
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        createNewUser(faroUser, "pfloyd782$");

        LoginHandler loginHandler = new LoginHandler();
        String token = loginHandler.login(faroUser.getEmail(), "pfloyd782$");

        Assert.assertNotNull(token);
    }

    public String createNewUser(FaroUserDo user, String password) {
        SignupHandler signupHandler = new SignupHandler();
        FaroUser faroUser = ConversionUtils.fromDo(user);
        return signupHandler.signupUser(new FaroSignupDetails(faroUser, password));
    }
}
