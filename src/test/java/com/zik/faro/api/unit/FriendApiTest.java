package com.zik.faro.api.unit;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.sun.jersey.api.JResponse;
import com.zik.faro.api.friends.FriendsHandler;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.FriendRelation;
import com.zik.faro.persistence.datastore.UserDatastoreImpl;

import org.junit.*;

import java.util.List;
import java.util.UUID;

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
        for(int i=0; i< userCount; i++) {
            FaroUser user = new FaroUser("user"+i+"@gmail.com",
                    "user"+i, null, "user"+i+"lname",
                    "user"+i+"@splitwise.com",
                    "0000000"+i,
                    new Address(44, "Abby Road", "SouthEnd London", "UK", 566645));
            UserDatastoreImpl.storeUser(user);
        }
        FriendsHandler friendsHandler = new FriendsHandler();
        friendsHandler.inviteFriend("user1@gmail.com", "user2@gmail.com");
        friendsHandler.inviteFriend("user1@gmail.com", "user2@gmail.com");
        friendsHandler.inviteFriend("user0@gmail.com", "user1@gmail.com");

        JResponse<List<FriendRelation>> relationList =
                friendsHandler.getFriends("user1@gmail.com");
        Assert.assertEquals(2, relationList.getEntity().size());
    }
}
