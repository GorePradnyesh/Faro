package com.zik.faro.functional;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.zik.faro.TestHelper;
import com.zik.faro.TestUtil;
import com.zik.faro.commons.FaroErrorResponse;
import com.zik.faro.data.Group;
import com.zik.faro.data.UpdateCollectionRequest;
import com.zik.faro.data.UpdateRequest;
import com.zik.faro.data.user.FaroUser;

public class FunctionalGroupTest {
	private static URL endpoint;
    private static String token = null;
    // User with whose creds we will be testing all the apis
    private static String callerEmail = UUID.randomUUID().toString() + "@gmail.com";
    private int noOfFaroUsers = 5;
    private FaroUser[] faroUsers = new FaroUser[noOfFaroUsers];
    
    @BeforeClass
    public static void init() throws Exception {
        endpoint = TestHelper.getExternalTargetEndpoint();
        token = TestHelper.createUserAndGetToken(callerEmail);
        FaroUser faroUser = new FaroUser(callerEmail, "sachin", "Vijay", "tendulkar", "testId", "1234456", null);
        faroUser.addToken("dnrr9xc1D6g:APA91bGJLMMHbtfVFw1FjfLmZq_pDx53UIbd-iKDDuIWllHVh05BcifGGvtFW_x5OyiqxPrOUUYTVoLyDS7IPfJVO3YvMjh3ECIlaP3WC4OdDl5QGIjzU7SRDt1fYt3Tkk8XfwOAWUML");
    	ClientResponse upsertResp = TestHelper.doPUT(endpoint.toString(), "v1/profile/upsert", token, faroUser);
        //token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0Njg1ODkwNzMsInVzZXJuYW1lIjoiNzY0ZmZlYjAtZGViMi00MTdhLTkyNzYtZWRkZmRiZGFkNzUwQGdtYWlsLmNvbSIsImVtYWlsIjoiNzY0ZmZlYjAtZGViMi00MTdhLTkyNzYtZWRkZmRiZGFkNzUwQGdtYWlsLmNvbSIsImlzcyI6ImZhcm8iLCJpYXQiOjE0NjM0MDUwNzN9.Ktv4YbXV8BrJsYSHdBikpEwINNF-q8iTLUBhzr9cVZA";
    }
    
    
    // Farousers to be part of the group
    private FaroUser[] createFaroUsers() throws Exception{
    	for(int i = 0; i < noOfFaroUsers; i++){
    		String name = "Kaivan"+UUID.randomUUID().toString();
    		FaroUser faroUser = new FaroUser(UUID.randomUUID().toString() + "@gmail.com", name, 
    				"Vijay", "tendulkar", "testId", "1234456", null);
    		TestHelper.createUser(faroUser.getId(), faroUser.getFirstName(), faroUser.getLastName(), faroUser.getMiddleName());
    		faroUsers[i] = faroUser;
    	}
    	return faroUsers;
    }
    
    
    @Test
    public void crudGroupTest() throws Exception{
    	Group group = new Group();
    	group.setGroupName("Faro");
    	createFaroUsers();
    	Set<String> participants = new HashSet<>();
    	for(int i = 0;  i < noOfFaroUsers; i++){
    		participants.add(faroUsers[i].getId());
    	}
    	Set<String> admins = new HashSet<>();
    	admins.add(callerEmail);participants.add(callerEmail);
    	
    	group.setAdmins(admins);
    	group.setParticipants(participants);
    	
    	// Create
    	ClientResponse createResponse = TestHelper.doPOST(endpoint.toString(), "v1/group/create", token, group);
    	Group createGroupResponse = createResponse.getEntity(Group.class);
    	
    	Assert.assertEquals(group.getGroupName(), createGroupResponse.getGroupName());
    	Assert.assertTrue(TestUtil.isEqualList(new ArrayList<>(group.getAdmins()), new ArrayList<>(createGroupResponse.getAdmins())));
    	Assert.assertTrue(TestUtil.isEqualList(new ArrayList<>(group.getParticipants()), new ArrayList<>(createGroupResponse.getParticipants())));
    	Assert.assertNotNull(createGroupResponse.getId());
    	Assert.assertEquals(1L, createGroupResponse.getVersion().longValue());
    	
    	// Read
    	ClientResponse getResponse = TestHelper.doGET(endpoint.toString(), "v1/group/"+createGroupResponse.getId()+"/details", new MultivaluedMapImpl(), token);
    	Group readGroupResponse = getResponse.getEntity(Group.class);
    	Assert.assertEquals(group.getGroupName(), readGroupResponse.getGroupName());
    	Assert.assertTrue(TestUtil.isEqualList(new ArrayList<>(group.getAdmins()), new ArrayList<>(readGroupResponse.getAdmins())));
    	Assert.assertTrue(TestUtil.isEqualList(new ArrayList<>(group.getParticipants()), new ArrayList<>(readGroupResponse.getParticipants())));
    	Assert.assertNotNull(readGroupResponse.getId());
    	Assert.assertEquals(1L, readGroupResponse.getVersion().longValue());
    	
    	// Update
    	readGroupResponse.setGroupName("FaroUpdated");
    	UpdateRequest<Group> updateGroupRequest = new UpdateRequest<>();
    	updateGroupRequest.addUpdatedFields("groupName");
    	updateGroupRequest.setUpdate(readGroupResponse);
    	
    	ClientResponse updateResponse = TestHelper.doPUT(endpoint.toString(), "v1/group/"+readGroupResponse.getId()+"/update", token, updateGroupRequest);
    	Group updateGroupResponse = updateResponse.getEntity(Group.class);
    	Assert.assertEquals(readGroupResponse.getGroupName(), updateGroupResponse.getGroupName());
    	Assert.assertEquals(2L, updateGroupResponse.getVersion().longValue());
    	
    	// Update participants(add and remove)
    	UpdateCollectionRequest<Group, String> updateParticipants = new UpdateCollectionRequest<>();
    	
    	List<String> toBeAdded = new ArrayList<String>();
    	toBeAdded.add("NewUserNotStillOnFaro@gmail.com");
    	FaroUser faroUserOnFaro = new FaroUser(UUID.randomUUID().toString()+"faroUserOnFaro@gmail.com", "sachin", "Vijay", "tendulkar", "testId", "1234456", null);
    	TestHelper.createUser(faroUserOnFaro.getId(), faroUserOnFaro.getFirstName(), faroUserOnFaro.getLastName(), faroUserOnFaro.getMiddleName());
    	toBeAdded.add(faroUserOnFaro.getId());
    	
    	List<String> toBeRemoved = new ArrayList<>();
    	toBeRemoved.add(faroUsers[3].getId());
    	
    	updateParticipants.setToBeAdded(toBeAdded);
    	updateParticipants.setToBeRemoved(toBeRemoved);
    	updateParticipants.setUpdate(updateGroupResponse);
    	
    	ClientResponse updateParticipantsResponse = TestHelper.doPUT(endpoint.toString(), "v1/group/"+updateGroupResponse.getId()+"/updateParticipants", token, updateParticipants);
    	Group updateParticipantsGroupResponse = updateParticipantsResponse.getEntity(Group.class);
    	toBeAdded.addAll(new ArrayList<String>(readGroupResponse.getParticipants()));
    	toBeAdded.removeAll(toBeRemoved);
    	Assert.assertTrue(TestUtil.isEqualList(toBeAdded, new ArrayList<String>(updateParticipantsGroupResponse.getParticipants())));
    	
    	// Update admins
    	toBeAdded.clear(); toBeRemoved.clear();
    	toBeAdded.add(faroUsers[0].getId());toBeAdded.add(faroUsers[1].getId());
    	toBeRemoved.add(callerEmail);
    	
    	UpdateCollectionRequest<Group, String> updateAdmins = new UpdateCollectionRequest<>();
    	updateAdmins.setToBeAdded(toBeAdded);
    	updateAdmins.setToBeRemoved(toBeRemoved);
    	updateAdmins.setUpdate(updateParticipantsGroupResponse);
    	ClientResponse updateAdminsResponse = TestHelper.doPUT(endpoint.toString(), "v1/group/"+updateGroupResponse.getId()+"/updateAdmins", token, updateAdmins);
    	Group updateAdminsGroupResponse = updateAdminsResponse.getEntity(Group.class);
    	Assert.assertTrue(TestUtil.isEqualList(toBeAdded, new ArrayList<String>(updateAdminsGroupResponse.getAdmins())));
    }
    
    @Test
    public void onlyAdminCanAddParticipants() throws Exception{
    	Group group = new Group();
    	group.setGroupName("Faro");
    	createFaroUsers();
    	Set<String> participants = new HashSet<>();
    	for(int i = 0;  i < noOfFaroUsers; i++){
    		participants.add(faroUsers[i].getId());
    	}
    	Set<String> admins = new HashSet<>();
    	admins.add(callerEmail);participants.add(callerEmail);
    	
    	group.setAdmins(admins);
    	group.setParticipants(participants);
    	
    	// Create
    	ClientResponse createResponse = TestHelper.doPOST(endpoint.toString(), "v1/group/create", token, group);
    	Group createGroupResponse = createResponse.getEntity(Group.class);
    	
    	Assert.assertEquals(group.getGroupName(), createGroupResponse.getGroupName());
    	Assert.assertTrue(TestUtil.isEqualList(new ArrayList<>(group.getAdmins()), new ArrayList<>(createGroupResponse.getAdmins())));
    	Assert.assertTrue(TestUtil.isEqualList(new ArrayList<>(group.getParticipants()), new ArrayList<>(createGroupResponse.getParticipants())));
    	Assert.assertNotNull(createGroupResponse.getId());
    	Assert.assertEquals(1L, createGroupResponse.getVersion().longValue());
    	
    	// Add new participants
    	List<String> toBeAdded = new ArrayList<>();
    	List<String> toBeRemoved = new ArrayList<>();
    	toBeAdded.add("aNewUser@email.com");
    	toBeRemoved.add(faroUsers[4].getId());
    	UpdateCollectionRequest<Group, String> updateParticipants= new UpdateCollectionRequest<>();
    	updateParticipants.setToBeAdded(toBeAdded);
    	updateParticipants.setToBeRemoved(toBeRemoved);
    	updateParticipants.setUpdate(createGroupResponse);
    	ClientResponse updateParticipantsResponse = TestHelper.doPUT(endpoint.toString(), "v1/group/"+createGroupResponse.getId()+"/updateParticipants", token, updateParticipants);
    	Group updateParticipantsGroupResponse = updateParticipantsResponse.getEntity(Group.class);
    	List<String> expected = new ArrayList<>();
    	for(int i = 0 ; i < faroUsers.length-1; i++){
    		expected.add(faroUsers[i].getId());
    	}
    	expected.addAll(toBeAdded);expected.add(callerEmail);
    	Assert.assertTrue(TestUtil.isEqualList(expected, new ArrayList<String>(updateParticipantsGroupResponse.getParticipants())));
    	
    	// Remove existing admin and add new admin
    	
    	toBeAdded.clear();
    	toBeRemoved.clear();
    	toBeAdded.add(faroUsers[0].getId());toBeAdded.add(faroUsers[1].getId());
    	toBeRemoved.add(callerEmail);
    	
    	UpdateCollectionRequest<Group, String> updateAdmins = new UpdateCollectionRequest<>();
    	updateAdmins.setToBeAdded(toBeAdded);
    	updateAdmins.setToBeRemoved(toBeRemoved);
    	updateAdmins.setUpdate(updateParticipantsGroupResponse);
    	ClientResponse updateAdminsResponse = TestHelper.doPUT(endpoint.toString(), "v1/group/"+createGroupResponse.getId()+"/updateAdmins", token, updateAdmins);
    	Group updateAdminsGroupResponse = updateAdminsResponse.getEntity(Group.class);
    	Assert.assertTrue(TestUtil.isEqualList(toBeAdded, new ArrayList<String>(updateAdminsGroupResponse.getAdmins())));
    	
    	// Try and add participants using older admin's token
    	updateParticipants = new UpdateCollectionRequest<>();
    	toBeAdded.clear();toBeAdded.add("userNotOnFaro");
    	updateParticipants.setToBeAdded(toBeAdded);
    	updateParticipants.setUpdate(updateAdminsGroupResponse);
    	
    	updateParticipantsResponse = TestHelper.doPUT(endpoint.toString(), "v1/group/"+updateAdminsGroupResponse.getId()+"/updateParticipants", token, updateParticipants);
    	Assert.assertEquals(401,updateParticipantsResponse.getStatus());
		FaroErrorResponse errorResponse = updateParticipantsResponse.getEntity(FaroErrorResponse.class);
    	Assert.assertEquals(3002, errorResponse.getFaroStatusCode());
    	
    	
    }
}
