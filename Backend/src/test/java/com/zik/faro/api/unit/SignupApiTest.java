package com.zik.faro.api.unit;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.api.authentication.SignupHandler;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.applogic.UserManagement;
import com.zik.faro.commons.exceptions.BadRequestException;
import com.zik.faro.commons.exceptions.EntityAlreadyExistsException;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.UserCredentials;
import com.zik.faro.persistence.datastore.UserCredentialsDatastoreImpl;
import org.junit.*;

import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * Created by granganathan on 2/8/15.
 */
@Ignore
public class SignupApiTest {
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
    public void testSignupUser() {
        final String fName = UUID.randomUUID().toString();
        FaroUser user = new FaroUser("rwaters@gmail.com",
                fName, null, "waters",
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        String password = "password@#$89";

        Response response = createNewUser(user, password);

        // Verify the response was no content response
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        // Verify the user exists and credentials have been entered
        Assert.assertTrue(UserManagement.isExistingUser(user.getId()));
        UserCredentials userCredentials = UserCredentialsDatastoreImpl.loadUserCreds(user.getId());
        Assert.assertNotNull(userCredentials);
        userCredentials.getEmail();
        userCredentials.getPassword();
        Assert.assertEquals(user.getId(), userCredentials.getEmail());
        Assert.assertNotNull(userCredentials.getPassword());
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void testSignupExistingUser() {
        final String fName = UUID.randomUUID().toString();
        FaroUser user = new FaroUser("rwaters@gmail.com",
                fName, null, "waters",
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        String password = "password@#$89";
        createNewUser(user, password);

        createNewUser(user, password);
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidArgsNullSignupDetails() {
        SignupHandler signupHandler = new SignupHandler();
        signupHandler.signupUser(null);
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidArgsNullFaroUser() {
        createNewUser(null, "password");
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidArgsNullPassword() {
        final String fName = UUID.randomUUID().toString();
        FaroUser user = new FaroUser("rwaters@gmail.com",
                fName, null, "waters",
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        createNewUser(user, null);
    }

    public Response createNewUser(FaroUser user, String password) {
        SignupHandler signupHandler = new SignupHandler();
        //return signupHandler.signupUser(new FaroSignupDetails(user, password));
        return null;
    }

}
