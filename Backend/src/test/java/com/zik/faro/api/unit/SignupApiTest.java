package com.zik.faro.api.unit;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.api.authentication.SignupHandler;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.applogic.UserManagement;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
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
    public void testSignupUser() throws DataNotFoundException {
        final String fName = UUID.randomUUID().toString();
        FaroUser user = new FaroUser("rwaters@gmail.com",
                fName, null, "waters",
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        String password = "password@#$89";

        String token = createNewUser(user, password);

        // Verify the response
        Assert.assertNotNull(token);

        // Verify the user exists and credentials have been entered
        Assert.assertTrue(UserManagement.isExistingUser(user.getEmail()));
        UserCredentials userCredentials = UserCredentialsDatastoreImpl.loadUserCreds(user.getEmail());
        Assert.assertNotNull(userCredentials);
        userCredentials.getEmail();
        userCredentials.getEncryptedPassword();
        Assert.assertEquals(user.getEmail(), userCredentials.getEmail());
        Assert.assertNotNull(userCredentials.getEncryptedPassword());
    }

    @Test
    public void testSignupExistingUser() {
        final String fName = UUID.randomUUID().toString();
        FaroUser user = new FaroUser(fName+ "@gmail.com",
                fName, null, "waters",
                fName + "@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        String password = "password@#$89";
        createNewUser(user, password);

        try {
            createNewUser(user, password);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FaroWebAppException);
            FaroWebAppException faroWebAppException = (FaroWebAppException)e;
            Assert.assertEquals(FaroResponseStatus.ENTITY_EXISTS, faroWebAppException.getFaroResponseStatus());
        }
    }

    @Test
    public void testInvalidArgsNullSignupDetails() {
        SignupHandler signupHandler = new SignupHandler();
        try {
            signupHandler.signupUser(null);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FaroWebAppException);
            FaroWebAppException faroWebAppException = (FaroWebAppException)e;
            Assert.assertEquals(FaroResponseStatus.BAD_REQUEST, faroWebAppException.getFaroResponseStatus());
        }

    }

    @Test
    public void testInvalidArgsNullFaroUser() {
        try {
            createNewUser(null, "password");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FaroWebAppException);
            FaroWebAppException faroWebAppException = (FaroWebAppException)e;
            Assert.assertEquals(FaroResponseStatus.BAD_REQUEST, faroWebAppException.getFaroResponseStatus());
        }
    }

    @Test
    public void testInvalidArgsNullPassword() {
        final String fName = UUID.randomUUID().toString();
        FaroUser user = new FaroUser("rwaters@gmail.com",
                fName, null, "waters",
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        try {
            createNewUser(user, null);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FaroWebAppException);
            FaroWebAppException faroWebAppException = (FaroWebAppException)e;
            Assert.assertEquals(FaroResponseStatus.BAD_REQUEST, faroWebAppException.getFaroResponseStatus());
        }
    }

    public static String createNewUser(FaroUser user, String password) {
        return new SignupHandler().signupUser(new FaroSignupDetails(user, password)).getEntity();
    }

}
