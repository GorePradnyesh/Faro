package com.zik.faro.persistence.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.TestUtil;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.GeoPosition;
import com.zik.faro.data.Identifier;
import com.zik.faro.data.Location;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.expense.ExpenseGroup;
import com.zik.faro.data.user.Address;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.GroupDo;
import com.zik.faro.persistence.datastore.data.GroupUserDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class GroupDatastoreImplTest {

    private static final LocalServiceTestHelper helper =
    		new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    private static final LocalServiceTestHelper helperWithHighReplication =
    		new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
    		.setDefaultHighRepJobPolicyUnappliedJobPercentage(100));
    

    static{
        ObjectifyService.register(GroupDo.class);
        ObjectifyService.register(FaroUserDo.class);
        ObjectifyService.register(GroupUserDo.class);
    }

    @BeforeClass
    public static void init(){
        ObjectifyService.begin();       // This is needed to set up the ofy service.
    }

    
    private FaroUserDo[] createFaroUsers(int count){
    	FaroUserDo[] faroUserArr = new FaroUserDo[count];
    	for(int i = 0 ; i < count; i++){
    		FaroUserDo faroUser = new FaroUserDo(Identifier.createUniqueIdentifierString()+"@gmail.com", "SampleUser", null, "Gilmour", "dg@splitwise.com",
                    "2323", new Address(123, "Palm Avenue", "Stanford", "CA", 94332));
    		DatastoreObjectifyDAL.storeObject(faroUser);
    		faroUserArr[i] = faroUser;
    	}
    	return faroUserArr;
    }
    
    private GroupDo createGroup(FaroUserDo[] faroUsers){
    	GroupDo group = new GroupDo();
    	group.setId(Identifier.createUniqueIdentifierString());
    	group.setGroupName("Test group");
    	Set<String> admins = new HashSet<String>();
    	admins.add(faroUsers[0].getId());admins.add(faroUsers[1].getId());
    	Set<String> participants = new HashSet<String>();
    	participants.add(faroUsers[0].getId());participants.add(faroUsers[1].getId());
    	participants.add(faroUsers[2].getId());participants.add(faroUsers[3].getId());
    	group.setAdmins(admins);
    	group.setParticipants(participants);
    	return group;
    }

    @Test
    public void testSimpleStoreLoad() throws DataNotFoundException{
    	helper.setUp();
    	// Create FaroUsers
    	FaroUserDo[] faroUsers = createFaroUsers(4);
    	
    	// Create Group
    	GroupDo group = createGroup(faroUsers);
   
    	GroupDatastoreImpl.storeGroup(group);
    	GroupDo loadGroup = GroupDatastoreImpl.loadGroupById(group.getId());
    	
    	Assert.assertEquals(group.getGroupName(), loadGroup.getGroupName());
    	Assert.assertEquals(group.getImage(), loadGroup.getImage());
    	Assert.assertEquals(group.getVersion(), loadGroup.getVersion());
    	TestUtil.isEqualList(new ArrayList<String>(group.getAdmins()), new ArrayList<String>(loadGroup.getAdmins()));
    	TestUtil.isEqualList(new ArrayList<String>(group.getParticipants()), new ArrayList<String>(loadGroup.getParticipants()));
    	
    	// Verify appropriate group users were created
    	for(String id: group.getParticipants()){
    		GroupUserDo groupUser = GroupUserDatastoreImpl.loadGroupUser(group.getId(), id);
    		Assert.assertNotNull(groupUser);
    		Assert.assertEquals(id, groupUser.getFaroUserDo().getId());
    		// Fetch based on farouserId
    		List<GroupUserDo> groupUserList = GroupUserDatastoreImpl.loadGroupUserByFaroUserId(id);
    		Assert.assertEquals(groupUserList.size(), 1);
    		Assert.assertEquals(id, groupUserList.get(0).getFaroUserDo().getId());
    		Assert.assertEquals(group.getId(), groupUserList.get(0).getGroupDo().getId());   		
    		// Fetch based on groupId
    		groupUserList = GroupUserDatastoreImpl.loadGroupUserByGroupId(group.getId());
    		Assert.assertEquals(groupUserList.size(), 4);
    	}
    	helper.tearDown();
    }
    
    @Test
    public void deleteGroupTest() throws DataNotFoundException{
    	helper.setUp();
    	FaroUserDo[] faroUsers = createFaroUsers(4);
    	// Create Group
    	GroupDo group = createGroup(faroUsers);
   
    	GroupDatastoreImpl.storeGroup(group);
    	GroupDo loadGroup = GroupDatastoreImpl.loadGroupById(group.getId());
    	Assert.assertEquals(group.getGroupName(), loadGroup.getGroupName());
    	Assert.assertEquals(group.getImage(), loadGroup.getImage());
    	Assert.assertEquals(group.getVersion(), loadGroup.getVersion());
    	TestUtil.isEqualList(new ArrayList<String>(group.getAdmins()), new ArrayList<String>(loadGroup.getAdmins()));
    	TestUtil.isEqualList(new ArrayList<String>(group.getParticipants()), new ArrayList<String>(loadGroup.getParticipants()));
    	
    	// Delete
    	GroupDatastoreImpl.deleteGroup(group.getId());
    	
    	// Verify delete
    	try{
    		loadGroup = GroupDatastoreImpl.loadGroupById(group.getId());
    	}catch(DataNotFoundException e){
    		Assert.assertNotNull(e);
    		return;
    	}
    	Assert.assertTrue("This should not happen", false);
    	helper.tearDown();
    }
    
    @Test
    public void updateGroupTest() throws DataNotFoundException, DatastoreException, UpdateVersionException, UpdateException{
    	
    	helperWithHighReplication.setUp();
    	FaroUserDo[] faroUsers = createFaroUsers(4);
    	// Create Group
    	GroupDo group = createGroup(faroUsers);
   
    	GroupDatastoreImpl.storeGroup(group);
    	GroupDo loadGroup = GroupDatastoreImpl.loadGroupById(group.getId());
    	Assert.assertEquals(group.getGroupName(), loadGroup.getGroupName());
    	Assert.assertEquals(group.getImage(), loadGroup.getImage());
    	Assert.assertEquals(group.getVersion(), loadGroup.getVersion());
    	TestUtil.isEqualList(new ArrayList<String>(group.getAdmins()), new ArrayList<String>(loadGroup.getAdmins()));
    	TestUtil.isEqualList(new ArrayList<String>(group.getParticipants()), new ArrayList<String>(loadGroup.getParticipants()));
    	
    	// Update group name
    	loadGroup.setGroupName("Updated group name");
    	
    	GroupDatastoreImpl.updateGroup(loadGroup.getId(), loadGroup, new HashSet<>(Arrays.asList("groupName")));
    	GroupDo updatedGroup = GroupDatastoreImpl.loadGroupById(group.getId());
    	Assert.assertEquals(loadGroup.getGroupName(), updatedGroup.getGroupName());
    	Assert.assertEquals(loadGroup.getImage(), updatedGroup.getImage());
    	Assert.assertEquals(loadGroup.getVersion().longValue()+1, updatedGroup.getVersion().longValue());
    	TestUtil.isEqualList(new ArrayList<String>(loadGroup.getAdmins()), new ArrayList<String>(updatedGroup.getAdmins()));
    	TestUtil.isEqualList(new ArrayList<String>(loadGroup.getParticipants()), new ArrayList<String>(updatedGroup.getParticipants()));
    	helperWithHighReplication.tearDown();
    }
    
    @Test
    public void updateGroupParticipants() throws DataNotFoundException, DatastoreException, UpdateVersionException, UpdateException{
    	helperWithHighReplication.setUp();
    	FaroUserDo[] faroUsers = createFaroUsers(4);
    	// Create Group
    	GroupDo group = createGroup(faroUsers);
   
    	GroupDatastoreImpl.storeGroup(group);
    	GroupDo loadGroup = GroupDatastoreImpl.loadGroupById(group.getId());
    	Assert.assertEquals(group.getGroupName(), loadGroup.getGroupName());
    	Assert.assertEquals(group.getImage(), loadGroup.getImage());
    	Assert.assertEquals(group.getVersion(), loadGroup.getVersion());
    	TestUtil.isEqualList(new ArrayList<String>(group.getAdmins()), new ArrayList<String>(loadGroup.getAdmins()));
    	TestUtil.isEqualList(new ArrayList<String>(group.getParticipants()), new ArrayList<String>(loadGroup.getParticipants()));
    	
    	// Add participants
    	FaroUserDo[] moreFaroUsers = createFaroUsers(4);
    	List<String> toBeAdded = new ArrayList<String>();
    	for(int i = 0 ; i < moreFaroUsers.length; i++){
    		toBeAdded.add(moreFaroUsers[i].getId());
    	}
    	GroupDo updatedGroup = GroupDatastoreImpl.updateParticipants(toBeAdded, null, 
    			loadGroup.getId(), loadGroup.getVersion());
    	Assert.assertEquals(loadGroup.getGroupName(), updatedGroup.getGroupName());
    	Assert.assertEquals(loadGroup.getImage(), updatedGroup.getImage());
    	Assert.assertEquals(loadGroup.getVersion().longValue()+1, updatedGroup.getVersion().longValue());
    	TestUtil.isEqualList(new ArrayList<String>(loadGroup.getAdmins()), new ArrayList<String>(updatedGroup.getAdmins()));
    	List<String> totalFaroUsers = new ArrayList<String>();
    	totalFaroUsers.addAll(loadGroup.getParticipants());totalFaroUsers.addAll(toBeAdded);
    	TestUtil.isEqualList(totalFaroUsers, new ArrayList<String>(updatedGroup.getParticipants()));
    	
    	// Remove participants
    	List<String> toBeRemoved = new ArrayList<String>();
    	toBeRemoved.add(totalFaroUsers.get(0));
    	
    	List<String> listPostRemoval = totalFaroUsers.subList(1, totalFaroUsers.size()-1); 
    	
    	GroupDo updatedGroup2 = GroupDatastoreImpl.updateParticipants(null, toBeRemoved, 
    			updatedGroup.getId(), updatedGroup.getVersion());
    	Assert.assertEquals(updatedGroup.getGroupName(), updatedGroup2.getGroupName());
    	Assert.assertEquals(updatedGroup.getImage(), updatedGroup2.getImage());
    	Assert.assertEquals(updatedGroup.getVersion().longValue()+1, updatedGroup2.getVersion().longValue());
    	TestUtil.isEqualList(new ArrayList<String>(loadGroup.getAdmins()), new ArrayList<String>(updatedGroup.getAdmins()));
    	TestUtil.isEqualList(listPostRemoval, new ArrayList<String>(updatedGroup2.getParticipants()));
    	
    	// Add and Remove
    	toBeRemoved.clear();toBeAdded.clear();listPostRemoval.clear();
    	// Add participants
    	moreFaroUsers = createFaroUsers(4);
    	toBeAdded = new ArrayList<String>();
    	for(int i = 0 ; i < moreFaroUsers.length; i++){
    		toBeAdded.add(moreFaroUsers[i].getId());
    	}
    	
    	List<String> currentParticipants = new ArrayList<String>(updatedGroup2.getParticipants());
    	toBeRemoved.add(currentParticipants.get(0));
    	listPostRemoval = currentParticipants.subList(1, currentParticipants.size()-1);
    	listPostRemoval.addAll(toBeAdded);
    	
    	GroupDo updatedGroup3 = GroupDatastoreImpl.updateParticipants(toBeAdded, toBeRemoved, 
    			updatedGroup2.getId(), updatedGroup2.getVersion());
    	Assert.assertEquals(updatedGroup2.getGroupName(), updatedGroup3.getGroupName());
    	Assert.assertEquals(updatedGroup2.getImage(), updatedGroup3.getImage());
    	Assert.assertEquals(updatedGroup2.getVersion().longValue()+1, updatedGroup3.getVersion().longValue());
    	TestUtil.isEqualList(new ArrayList<String>(updatedGroup2.getAdmins()), new ArrayList<String>(updatedGroup3.getAdmins()));
    	TestUtil.isEqualList(listPostRemoval, new ArrayList<String>(updatedGroup3.getParticipants()));
    	helperWithHighReplication.tearDown();
    }
    
    @Test
    public void updateGroupAdmins() throws DataNotFoundException, DatastoreException, UpdateVersionException, UpdateException{
    	helperWithHighReplication.setUp();
    	FaroUserDo[] faroUsers = createFaroUsers(4);
    	// Create Group
    	GroupDo group = createGroup(faroUsers);
   
    	GroupDatastoreImpl.storeGroup(group);
    	GroupDo loadGroup = GroupDatastoreImpl.loadGroupById(group.getId());
    	Assert.assertEquals(group.getGroupName(), loadGroup.getGroupName());
    	Assert.assertEquals(group.getImage(), loadGroup.getImage());
    	Assert.assertEquals(group.getVersion(), loadGroup.getVersion());
    	TestUtil.isEqualList(new ArrayList<String>(group.getAdmins()), new ArrayList<String>(loadGroup.getAdmins()));
    	TestUtil.isEqualList(new ArrayList<String>(group.getParticipants()), new ArrayList<String>(loadGroup.getParticipants()));
    	
    	// Add admins
    	List<String> toBeAdded = new ArrayList<String>();
    	List<String> currentParticipants = new ArrayList<String>();
    	for(FaroUserDo faroUser : faroUsers){
    		currentParticipants.add(faroUser.getId());
    	}
    	List<String> currentAdmins = new ArrayList<String>();
    	
    	// 0 and 1 are already admins
    	currentAdmins.add(currentParticipants.get(0));
    	currentAdmins.add(currentParticipants.get(1));
    	
    	// Make 2 as admin
    	toBeAdded.add(currentParticipants.get(2));
    	
    	GroupDo updatedGroup = GroupDatastoreImpl.updateParticipants(toBeAdded, null, 
    			loadGroup.getId(), loadGroup.getVersion());
    	Assert.assertEquals(loadGroup.getGroupName(), updatedGroup.getGroupName());
    	Assert.assertEquals(loadGroup.getImage(), updatedGroup.getImage());
    	Assert.assertEquals(loadGroup.getVersion().longValue()+1, updatedGroup.getVersion().longValue());
    	TestUtil.isEqualList(new ArrayList<String>(loadGroup.getParticipants()), new ArrayList<String>(updatedGroup.getParticipants()));
    	currentAdmins.add(currentParticipants.get(2));
    	
    	TestUtil.isEqualList(currentAdmins, new ArrayList<String>(updatedGroup.getAdmins()));
    	
    	// Remove admins
    	List<String> toBeRemoved = new ArrayList<String>();
    	toBeRemoved.add(currentParticipants.get(0));
    	
    	GroupDo updatedGroup2 = GroupDatastoreImpl.updateParticipants(null, toBeRemoved, 
    			updatedGroup.getId(), updatedGroup.getVersion());
    	Assert.assertEquals(updatedGroup.getGroupName(), updatedGroup2.getGroupName());
    	Assert.assertEquals(updatedGroup.getImage(), updatedGroup2.getImage());
    	Assert.assertEquals(updatedGroup.getVersion().longValue()+1, updatedGroup2.getVersion().longValue());
    	
    	currentAdmins.remove(0);
    	TestUtil.isEqualList(currentAdmins, new ArrayList<String>(updatedGroup2.getAdmins()));
    	TestUtil.isEqualList(new ArrayList<String>(updatedGroup.getParticipants()), new ArrayList<String>(updatedGroup2.getParticipants()));
    	
    	// Add and Remove
    	toBeRemoved.clear();toBeAdded.clear();
    	
    	toBeAdded.add(currentParticipants.get(3));
    	toBeRemoved.add(currentParticipants.get(1));
    	toBeRemoved.add(currentParticipants.get(2));
    	
    	currentAdmins.clear();
    	currentAdmins.add(currentParticipants.get(3));
    	
    	GroupDo updatedGroup3 = GroupDatastoreImpl.updateParticipants(toBeAdded, toBeRemoved, 
    			updatedGroup2.getId(), updatedGroup2.getVersion());
    	
    	Assert.assertEquals(updatedGroup2.getGroupName(), updatedGroup3.getGroupName());
    	Assert.assertEquals(updatedGroup2.getImage(), updatedGroup3.getImage());
    	Assert.assertEquals(updatedGroup2.getVersion().longValue()+1, updatedGroup3.getVersion().longValue());
    	
    	TestUtil.isEqualList(new ArrayList<String>(updatedGroup2.getParticipants()), 
    			new ArrayList<String>(updatedGroup3.getParticipants()));
    	TestUtil.isEqualList(currentAdmins, new ArrayList<String>(updatedGroup3.getAdmins()));
    	helperWithHighReplication.tearDown();
    }
    
    
}
