package com.zik.faro.api.unit;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.sun.jersey.api.JResponse;
import com.zik.faro.TestHelper;
import com.zik.faro.api.friends.FriendsHandler;
import com.zik.faro.api.responder.MinUser;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.FriendRelation;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;

import org.junit.*;
import org.powermock.reflect.Whitebox;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

public class FriendApiTest {
    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(50));

    static{
        ObjectifyService.register(FaroUser.class);
        ObjectifyService.register(FriendRelation.class);
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
    public void testFriendRelation(){
        int userCount = 3;
        for(int i = 0; i< userCount; i++) {
            FaroUser user = new FaroUser("user"+i+"@gmail.com",
                    "user"+i, null, "user"+i+"lname",
                    "user"+i+"@splitwise.com",
                    "0000000"+i,
                    new Address(44, "Abby Road", "SouthEnd London", "UK", 566645));
            UserDatastoreImpl.storeUser(user);
        }
        FriendsHandler friendsHandler = new FriendsHandler();
        // Setup mock Security context for the handler
        Whitebox.setInternalState(friendsHandler, TestHelper.setupMockSecurityContext("user0@gmail.com"));

        // Invite 2 friends
        friendsHandler.inviteFriend("user2@gmail.com");
        friendsHandler.inviteFriend("user2@gmail.com");
        friendsHandler.inviteFriend("user1@gmail.com");

        // Verify inviting yourself results in bad request response status
        try {
            friendsHandler.inviteFriend("user0@gmail.com");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FaroWebAppException);
            FaroWebAppException faroWebAppException = (FaroWebAppException)e;
            Assert.assertEquals(faroWebAppException.getFaroResponseStatus().getRestResponseStatus(),
                    Response.Status.BAD_REQUEST);
        }

        JResponse<List<MinUser>> relationList = friendsHandler.getFriends();
        List<MinUser> minUsers1 = relationList.getEntity();
        Assert.assertEquals(2, minUsers1.size());
        for(MinUser minUser: minUsers1){
            if(minUser.email.equals("user2@gmail.com")){
                Assert.assertEquals("user2@splitwise.com", minUser.expenseUserId);
                Assert.assertEquals("user2", minUser.firstName);
                Assert.assertEquals("user2lname", minUser.lastName);
            }else if(minUser.email.equals("user0@gmail.com")){
                Assert.assertEquals("user0@splitwise.com", minUser.expenseUserId);
                Assert.assertEquals("user0", minUser.firstName);
                Assert.assertEquals("user0lname", minUser.lastName);
            } 
        }
    }

    @Test
    public void testDeleteFriendRelation(){
        int userCount = 3;
        for(int i=0; i< userCount; i++) {
            FaroUser user = new FaroUser("user"+i+"@gmail.com",
                    "user"+i, null, "user"+i+"lname",
                    "user"+i+"@splitwise.com",
                    "0000000"+i,
                    new Address(44, "Abby Road", "SouthEnd London", "UK", 566645));
            UserDatastoreImpl.storeUser(user);
        }
        FriendsHandler friendsHandler = new FriendsHandler();
        Whitebox.setInternalState(friendsHandler, TestHelper.setupMockSecurityContext("user0@gmail.com"));
        friendsHandler.inviteFriend("user1@gmail.com");
        friendsHandler.inviteFriend("user2@gmail.com");

        JResponse<List<MinUser>> relationList = friendsHandler.getFriends();
        List<MinUser> minUsers1 = relationList.getEntity();
        Assert.assertEquals(2, minUsers1.size());

        friendsHandler.unFriend("user1@gmail.com");
        JResponse<List<MinUser>> deletedRelationList =
                friendsHandler.getFriends();
        Assert.assertEquals(1, deletedRelationList.getEntity().size());

    }
}
