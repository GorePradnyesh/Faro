package com.zik.faro.api.unit;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.TestHelper;
import com.zik.faro.api.authentication.LoginHandler;
import com.zik.faro.api.authentication.PasswordHandler;
import com.zik.faro.api.authentication.SignupHandler;
import com.zik.faro.data.FaroSignupDetails;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroResetPasswordData;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.data.user.UserCredentialsDo;
import org.junit.*;
import org.powermock.reflect.Whitebox;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Properties;
import java.util.UUID;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by granganathan on 4/18/15.
 */
public class PasswordHandlerTest {
    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static {
        ObjectifyService.register(FaroUser.class);
        ObjectifyService.register(UserCredentialsDo.class);
    }

    @BeforeClass
    public static void init(){
        ObjectifyService.begin();       // This is needed to set up the ofy service.
        Properties properties = System.getProperties();
        properties.setProperty("unit-test", "true");
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
        FaroUser faroUser = new FaroUser("pacman@gmail.com",
                fName, null, "waters",
                "pacman@splitwise.com",
                "4085393212",
                new Address(28, "yoko","Tokyo","Japan", 128685));

        String token = createNewUser(faroUser, oldPassword);
        // Verify signup was successful
        Assert.assertNotNull(token);

        // Login
        LoginHandler loginHandler = new LoginHandler();
        String loginToken = loginHandler.login(faroUser.getEmail(), oldPassword);
        // Verify login was successful
        Assert.assertNotNull(loginToken);

        PasswordHandler passwordHandler =  new PasswordHandler();
        Whitebox.setInternalState(passwordHandler, TestHelper.setupMockSecurityContext(faroUser.getEmail()));
        passwordHandler.resetPassword(new FaroResetPasswordData(oldPassword, newPassword));

        // Verify login fails with old password
        try {
            loginHandler.login(faroUser.getEmail(), oldPassword);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FaroWebAppException);
            FaroWebAppException faroWebAppException = (FaroWebAppException)e;
            Assert.assertEquals(FaroResponseStatus.INVALID_LOGIN, faroWebAppException.getFaroResponseStatus());
        }

        // Verify login succeeds with new password
        loginToken = loginHandler.login(faroUser.getEmail(), newPassword);
        Assert.assertNotNull(loginToken);
    }

    @Test
    public void forgotPasswordTest() throws Exception {
        String oldPassword = "contra#!";
        String newPassword = "superMarioBros12$";
        final String fName = UUID.randomUUID().toString();
        FaroUser faroUser = new FaroUser("mario@gmail.com",
                fName, null, "waters",
                "mario@splitwise.com",
                "4085393212",
                new Address(44, "North","Castle","Italy", 566645));

        String token = createNewUser(faroUser, oldPassword);
        // Verify signup was successful
        Assert.assertNotNull(token);

        PasswordHandler passwordHandler =  new PasswordHandler();
        UriInfo mockedUriInfo = mock(UriInfo.class);
        when(mockedUriInfo.getBaseUri()).thenReturn(new URI("http://localhost:8080/v1/"));
        Whitebox.setInternalState(passwordHandler, mockedUriInfo);

        // Obtain the forgot password url
        String forgotPasswordUrl = passwordHandler.forgotPassword(faroUser.getEmail());
        Assert.assertNotNull(forgotPasswordUrl);
        System.out.println("forgot password url = " + forgotPasswordUrl);
        String queryParamToken = getQueryParamFromUrl(forgotPasswordUrl, "token");
        Assert.assertNotNull(token);

        // Obtain the HTML forgotPassword form
        String htmlForm = passwordHandler.forgotPasswordForm(queryParamToken);
        System.out.println("forgot password form html page = " + htmlForm);

        // Set a new password
        Whitebox.setInternalState(passwordHandler, TestHelper.setupMockSecurityContext(faroUser.getEmail()));


        passwordHandler.newPassword(newPassword);

        // Verify login succeeds with new password
        LoginHandler loginHandler = new LoginHandler();
        String loginToken = loginHandler.login(faroUser.getEmail(), newPassword);
        Assert.assertNotNull(loginToken);
    }

    private String createNewUser(FaroUser user, String password) {
        SignupHandler signupHandler = new SignupHandler();
        return signupHandler.signupUser(new FaroSignupDetails(user, password));
    }

    private String getQueryParamFromUrl(String url, String queryParamName) {
        int queryParamNameIndex = url.indexOf(queryParamName + "=") + queryParamName.length() + 1;

        return url.substring(queryParamNameIndex, url.length());
    }

}
