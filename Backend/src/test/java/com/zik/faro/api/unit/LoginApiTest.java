package com.zik.faro.api.unit;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.api.authentication.LoginHandler;
import com.zik.faro.api.authentication.SignupHandler;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.UserCredentials;
import org.junit.*;

import javax.ws.rs.core.Response;
import java.util.UUID;

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
        String token = loginHandler.login(faroUser.getId(), "pfloyd782$");

        Assert.assertNotNull(token);
    }

    public Response createNewUser(FaroUser user, String password) {
        SignupHandler signupHandler = new SignupHandler();
        return signupHandler.signupUser(new FaroSignupDetails(user, password));
    }
}
