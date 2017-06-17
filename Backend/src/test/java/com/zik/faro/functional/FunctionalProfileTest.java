package com.zik.faro.functional;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedMap;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.zik.faro.TestHelper;
import com.zik.faro.data.user.FaroUser;

public class FunctionalProfileTest {
	private static URL endpoint;
    private static String token = null;
    // User with whose creds we will be testing all the apis
    private static String callerEmail = UUID.randomUUID().toString() + "@gmail.com";
    
    @BeforeClass
    public static void init() throws Exception {
        endpoint = TestHelper.getExternalTargetEndpoint();
        token = TestHelper.createUserAndGetToken(callerEmail);
        //token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0Njg1ODkwNzMsInVzZXJuYW1lIjoiNzY0ZmZlYjAtZGViMi00MTdhLTkyNzYtZWRkZmRiZGFkNzUwQGdtYWlsLmNvbSIsImVtYWlsIjoiNzY0ZmZlYjAtZGViMi00MTdhLTkyNzYtZWRkZmRiZGFkNzUwQGdtYWlsLmNvbSIsImlzcyI6ImZhcm8iLCJpYXQiOjE0NjM0MDUwNzN9.Ktv4YbXV8BrJsYSHdBikpEwINNF-q8iTLUBhzr9cVZA";
    }
    
    private void createProfile(){
    	
    }
    
    private void updateProfile(){
    	
    }
    
    private void upsertProfile() throws IOException{
    	String randomEmail = UUID.randomUUID().toString() + "@gmail.com";
    	// Test insert part of upsert
    	FaroUser faroUser = new FaroUser(randomEmail, "Kaivan", "Vijay", "Sanghvi", "testId", "1234456", null);
    	ClientResponse upsertResp = TestHelper.doPUT(endpoint.toString(), "v1/profile/upsert", token, faroUser);
    	FaroUser resp = upsertResp.getEntity(FaroUser.class);
    	assertEntity(faroUser, resp);
    	
    	faroUser.setUserTopic("/topics/newtopic");
    	upsertResp = TestHelper.doPUT(endpoint.toString(), "v1/profile/upsert", token, faroUser);
    	resp = upsertResp.getEntity(FaroUser.class);
    	assertEntity(faroUser, resp);
    	
    }
    
    private void addRegistrationToken() throws IOException{
    	String randomEmail = UUID.randomUUID().toString() + "@gmail.com";
    	// Test insert part of upsert
    	FaroUser faroUser = new FaroUser(randomEmail, "Kaivan", "Vijay", "Sanghvi", "testId", "1234456", null);
    	ClientResponse upsertResp = TestHelper.doPUT(endpoint.toString(), "v1/profile/upsert", token, faroUser);
    	FaroUser resp = upsertResp.getEntity(FaroUser.class);
    	assertEntity(faroUser, resp);
    	
    	// Add a new registration token
    	String registrationToken = UUID.randomUUID().toString();
    	upsertResp = TestHelper.doPUT(endpoint.toString(), "v1/profile/add/registrationToken", token, registrationToken);
    	String httpOk = upsertResp.getEntity(String.class);
    	Assert.assertEquals("OK", httpOk);
    	Assert.assertEquals(200, upsertResp.getStatus());
    	
    	// Verify by fetching from backend
    	ClientResponse readProfile = TestHelper.doGET(endpoint.toString(), "v1/profile", new MultivaluedMapImpl(), token);
    	FaroUser profile = readProfile.getEntity(FaroUser.class);
    	Assert.assertTrue(profile.getTokens().contains(registrationToken));
    	
    	// Add another registration token
    	registrationToken = UUID.randomUUID().toString();
    	upsertResp = TestHelper.doPUT(endpoint.toString(), "v1/profile/add/registrationToken", token, registrationToken);
    	httpOk = upsertResp.getEntity(String.class);
    	Assert.assertEquals("OK", httpOk);
    	Assert.assertEquals(200, upsertResp.getStatus());
    	
    	// Verify by fetching from backend
    	readProfile = TestHelper.doGET(endpoint.toString(), "v1/profile", new MultivaluedMapImpl(), token);
    	profile = readProfile.getEntity(FaroUser.class);
    	Assert.assertTrue(profile.getTokens().contains(registrationToken));
    	
    	// Delete one of the tokens
    	upsertResp = TestHelper.doPUT(endpoint.toString(), "v1/profile/remove/registrationToken", token, registrationToken);
    	httpOk = upsertResp.getEntity(String.class);
    	Assert.assertEquals("OK", httpOk);
    	Assert.assertEquals(200, upsertResp.getStatus());
    	
    	// Verify by fetching from backend
    	readProfile = TestHelper.doGET(endpoint.toString(), "v1/profile", new MultivaluedMapImpl(), token);
    	profile = readProfile.getEntity(FaroUser.class);
    	Assert.assertFalse(profile.getTokens().contains(registrationToken));
    }
    
    private void assertEntity(FaroUser expected, FaroUser actual){
    	Assert.assertEquals(expected.getId(), actual.getId());
    	Assert.assertEquals(expected.getFirstName(), actual.getFirstName());
    	Assert.assertEquals(expected.getLastName(), actual.getLastName());
    	Assert.assertEquals(expected.getMiddleName(), actual.getMiddleName());
    	Assert.assertEquals(expected.getUserTopic(), actual.getUserTopic());
    }
   
    @Test
    public void allTest() throws Exception{
    	System.out.println(token);
    	upsertProfile();
    	addRegistrationToken();
    }
}
