package com.zik.faro.api.unit;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.TestHelper;
import com.zik.faro.api.authentication.LoginHandler;
import com.zik.faro.api.authentication.PasswordHandler;
import com.zik.faro.api.authentication.SignupHandler;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.UserCredentials;
import org.junit.*;
import org.powermock.reflect.Whitebox;

import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * Created by granganathan on 4/18/15.
 */
public class PasswordHandlerTest {
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
    public void resetPasswordTest() throws Exception {
        String oldPassword = "donkeykong45#!";
        String newPassword = "pacman0012$";
        final String fName = UUID.randomUUID().toString();
        FaroUser faroUser = new FaroUser("rwaters@gmail.com",
                fName, null, "waters",
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));

        String token = createNewUser(faroUser, oldPassword);
        // Verify signup was successful
        Assert.assertNotNull(token);

        // Login
        LoginHandler loginHandler = new LoginHandler();
        String loginToken = loginHandler.login(faroUser.getId(), oldPassword);
        // Verify login was successful
        Assert.assertNotNull(loginToken);

        PasswordHandler passwordHandler =  new PasswordHandler();
        Whitebox.setInternalState(passwordHandler, TestHelper.setupMockSecurityContext(faroUser.getId()));
        passwordHandler.resetPassword(oldPassword, newPassword);

        // Verify login fails with old password and succeeds wuth the new one
        try {
            loginHandler.login(faroUser.getId(), oldPassword);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FaroWebAppException);
            FaroWebAppException faroWebAppException = (FaroWebAppException)e;
            Assert.assertEquals(FaroResponseStatus.INVALID_LOGIN, faroWebAppException.getFaroResponseStatus());
        }

        Assert.assertNotNull(loginHandler.login(faroUser.getId(), newPassword));
    }

    public String createNewUser(FaroUser user, String password) {
        SignupHandler signupHandler = new SignupHandler();
        return signupHandler.signupUser(new FaroSignupDetails(user, password));
    }

}
