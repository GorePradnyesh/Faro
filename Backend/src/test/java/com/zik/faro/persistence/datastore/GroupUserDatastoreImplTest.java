package com.zik.faro.persistence.datastore;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.persistence.datastore.data.GroupDo;
import com.zik.faro.persistence.datastore.data.GroupUserDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class GroupUserDatastoreImplTest {
	
	private static final LocalServiceTestHelper helper =
    		new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
            );
    

    static{
        ObjectifyService.register(GroupDo.class);
        ObjectifyService.register(FaroUserDo.class);
        ObjectifyService.register(GroupUserDo.class);
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
    public void test(){
    	String groupName = "testGroup";
    	String groupName1 = "testGroup1";
    	String user1 = "testUser1";
    	String user2 = "testUser2";
    	GroupUserDatastoreImpl.storeGroupUser(groupName, user1);
    	GroupUserDatastoreImpl.storeGroupUser(groupName, user2);
    	GroupUserDatastoreImpl.storeGroupUser(groupName1, user1);
    	GroupUserDo test1 = GroupUserDatastoreImpl.loadGroupUser(groupName, user1);
    	GroupUserDo test2 = GroupUserDatastoreImpl.loadGroupUser(groupName, user2);
    	
    	Assert.assertEquals(user1, test1.getFaroUserRef().key().getName());
    	Assert.assertEquals(user2, test2.getFaroUserRef().key().getName());
    	Assert.assertEquals(groupName, test1.getGroupRef().key().getName());
    	Assert.assertEquals(groupName, test2.getGroupRef().key().getName());
    	
    	// Get users given a group
    	List<GroupUserDo> faroUsersPerGroup = GroupUserDatastoreImpl.loadGroupUserByGroupId(groupName);
    	Assert.assertEquals(2, faroUsersPerGroup.size());
    	Assert.assertEquals(user1, faroUsersPerGroup.get(0).getFaroUserRef().key().getName());
    	Assert.assertEquals(user2, faroUsersPerGroup.get(1).getFaroUserRef().key().getName());
    	
    	// Get users given a faro user
    	List<GroupUserDo> groupsPerUser = GroupUserDatastoreImpl.loadGroupUserByFaroUserId(user1);
    	Assert.assertEquals(2, groupsPerUser.size());
    	Assert.assertEquals(groupName, groupsPerUser.get(0).getGroupRef().key().getName());
    	Assert.assertEquals(groupName1, groupsPerUser.get(1).getGroupRef().key().getName());
    }
}
