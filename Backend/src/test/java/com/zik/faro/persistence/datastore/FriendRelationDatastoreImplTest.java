package com.zik.faro.persistence.datastore;


import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.api.responder.MinUser;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.IllegalDataOperation;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.FriendRelation;

import org.junit.*;

import java.util.List;

public class FriendRelationDatastoreImplTest {

    private static final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    static{
        ObjectifyService.register(FriendRelation.class);
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
    public void testFriendRelationConstructor() throws IllegalDataOperation {
        FaroUser user1 = new FaroUser("user1@gmail.com",
                "user", null, "1","user1@splitwise.com",
                "00000001", new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        FaroUser user2 = new FaroUser("user2@gmail.com",
                "user", null, "2","user2@splitwise.com",
                "00000002", new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        FriendRelation relation = new FriendRelation(user1.getEmail(), user2.getEmail(), user1.getFirstName(), user1.getLastName(), user1.getExternalExpenseID());
        Assert.assertEquals(user1.getEmail(), relation.getFromId());
        Assert.assertEquals(user2.getEmail(), relation.getToId());

        FriendRelation invertedRelation = new FriendRelation(user2.getEmail(), user1.getEmail(), user2.getFirstName(), user2.getLastName(), user2.getExternalExpenseID());
        Assert.assertEquals(user2.getEmail(), invertedRelation.getFromId());
        Assert.assertEquals(user1.getEmail(), invertedRelation.getToId());
    }

    @Test(expected = IllegalDataOperation.class)
    public void testFriendRelationConstructorSameId() throws IllegalDataOperation {
        FaroUser user1 = new FaroUser("user1@gmail.com",
                "user", null, "1","user1@splitwise.com",
                "00000001", new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        FaroUser user2 = new FaroUser("user1@gmail.com",
                "user", null, "1","user1@splitwise.com",
                "00000001", new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        new FriendRelation(user1.getEmail(), user2.getEmail(), user1.getFirstName(), user1.getLastName(), user1.getExternalExpenseID());
    }

    @Test
    public void testFriendRelation() throws IllegalDataOperation, DataNotFoundException {
        FaroUser user1 = new FaroUser("user1@gmail.com",
                "user", null, "1","user1@splitwise.com",
                "00000001", new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        FaroUser user2 = new FaroUser("user2@gmail.com",
                "user", null, "2","user2@splitwise.com",
                "00000002", new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        FaroUser user3 = new FaroUser("user3@gmail.com",
                "user", null, "3","user3@splitwise.com",
                "00000003", new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        FaroUser user4 = new FaroUser("user4@gmail.com",
                "user", null, "4","user4@splitwise.com",
                "00000004", new Address(44, "Abby Road","SouthEnd London","UK", 566645));

        /*
        1<-->2
        1<-->3
        1<-->4
        2<-->4
        3<-->4
        */
        FriendRelationDatastoreImpl.storeFriendRelation(
                new MinUser(user1.getFirstName(), user1.getLastName(), user1.getEmail(), user1.getExternalExpenseID()),
                new MinUser(user2.getFirstName(), user2.getLastName(), user2.getEmail(), user2.getExternalExpenseID()));
        FriendRelationDatastoreImpl.storeFriendRelation(
                new MinUser(user1.getFirstName(), user1.getLastName(), user1.getEmail(), user1.getExternalExpenseID()),
                new MinUser(user3.getFirstName(), user3.getLastName(), user3.getEmail(), user3.getExternalExpenseID()));
        FriendRelationDatastoreImpl.storeFriendRelation(
                new MinUser(user1.getFirstName(), user1.getLastName(), user1.getEmail(), user1.getExternalExpenseID()),
                new MinUser(user4.getFirstName(), user4.getLastName(), user4.getEmail(), user4.getExternalExpenseID()));
        FriendRelationDatastoreImpl.storeFriendRelation(
                new MinUser(user2.getFirstName(), user2.getLastName(), user2.getEmail(), user2.getExternalExpenseID()),
                new MinUser(user4.getFirstName(), user4.getLastName(), user4.getEmail(), user4.getExternalExpenseID()));
        FriendRelationDatastoreImpl.storeFriendRelation(
                new MinUser(user3.getFirstName(), user3.getLastName(), user3.getEmail(), user3.getExternalExpenseID()),
                new MinUser(user4.getFirstName(), user4.getLastName(), user4.getEmail(), user4.getExternalExpenseID()));

        List<FriendRelation> relationUser1 = FriendRelationDatastoreImpl.loadFriendsForUserId(user1.getEmail());
        Assert.assertEquals(3, relationUser1.size());
        List<FriendRelation> relationUser2 = FriendRelationDatastoreImpl.loadFriendsForUserId(user2.getEmail());
        Assert.assertEquals(2, relationUser2.size());
        List<FriendRelation> relationUser3 = FriendRelationDatastoreImpl.loadFriendsForUserId(user3.getEmail());
        Assert.assertEquals(2, relationUser3.size());
        List<FriendRelation> relationUser4 = FriendRelationDatastoreImpl.loadFriendsForUserId(user4.getEmail());
        Assert.assertEquals(3, relationUser4.size());

        FriendRelation relation12 = FriendRelationDatastoreImpl.loadFriendRelation(user1.getEmail(), user2.getEmail());
        Assert.assertNotNull(relation12);
        FriendRelation relation21 = FriendRelationDatastoreImpl.loadFriendRelation(user2.getEmail(), user1.getEmail());
        Assert.assertNotNull(relation21);

        FriendRelation relation23 = FriendRelationDatastoreImpl.loadFriendRelation(user2.getEmail(), user3.getEmail());
        Assert.assertNull(relation23);

    }

    @Test
    public void testDeleteFriendRelation() throws IllegalDataOperation {
        FaroUser user1 = new FaroUser("user1@gmail.com",
                "user", null, "1", "user1@splitwise.com",
                "00000001", new Address(44, "Abby Road", "SouthEnd London", "UK", 566645));
        FaroUser user2 = new FaroUser("user2@gmail.com",
                "user", null, "2", "user2@splitwise.com",
                "00000002", new Address(44, "Abby Road", "SouthEnd London", "UK", 566645));

        FriendRelationDatastoreImpl.storeFriendRelation(
                new MinUser(user1.getFirstName(), user1.getLastName(), user1.getEmail(), user1.getExternalExpenseID()),
                new MinUser(user2.getFirstName(), user2.getLastName(), user2.getEmail(), user2.getExternalExpenseID()));

        List<FriendRelation> relationUser1 = FriendRelationDatastoreImpl.loadFriendsForUserId(user1.getEmail());
        Assert.assertEquals(1, relationUser1.size());

        FriendRelationDatastoreImpl.deleteFriendRelation(user1.getId(), user2.getId());
        List<FriendRelation> deletedRelations = FriendRelationDatastoreImpl.loadFriendsForUserId(user1.getEmail());
        Assert.assertEquals(0, deletedRelations.size());
    }

    @Test
    public void testFriendRelationIdempotentStroate() throws IllegalDataOperation {
        FaroUser user1 = new FaroUser("user1@gmail.com",
                "user", null, "1","user1@splitwise.com",
                "00000001", new Address(44, "Abby Road","SouthEnd London","UK", 566645));
        FaroUser user2 = new FaroUser("user2@gmail.com",
                "user", null, "2","user2@splitwise.com",
                "00000002", new Address(44, "Abby Road","SouthEnd London","UK", 566645));

        /* Repeat 'n' times */
        FriendRelationDatastoreImpl.storeFriendRelation(
                new MinUser(user1.getFirstName(), user1.getLastName(), user1.getEmail(), user1.getExternalExpenseID()),
                new MinUser(user2.getFirstName(), user2.getLastName(), user2.getEmail(), user2.getExternalExpenseID()));
        FriendRelationDatastoreImpl.storeFriendRelation(
                new MinUser(user1.getFirstName(), user1.getLastName(), user1.getEmail(), user1.getExternalExpenseID()),
                new MinUser(user2.getFirstName(), user2.getLastName(), user2.getEmail(), user2.getExternalExpenseID()));
        FriendRelationDatastoreImpl.storeFriendRelation(
                new MinUser(user1.getFirstName(), user1.getLastName(), user1.getEmail(), user1.getExternalExpenseID()),
                new MinUser(user2.getFirstName(), user2.getLastName(), user2.getEmail(), user2.getExternalExpenseID()));

        List<FriendRelation> relationUser1 = FriendRelationDatastoreImpl.loadFriendsForUserId(user1.getEmail());
        Assert.assertEquals(1, relationUser1.size());
    }

}
