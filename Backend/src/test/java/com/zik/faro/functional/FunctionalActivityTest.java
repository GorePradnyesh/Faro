package com.zik.faro.functional;

import java.net.URL;
import java.util.Calendar;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.zik.faro.TestHelper;
import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Location;

public class FunctionalActivityTest {
	private static URL endpoint;
    private static String token = null;

    @BeforeClass
    public static void init() throws Exception {
        endpoint = TestHelper.getExternalTargetEndpoint();
        token = TestHelper.createUserAndGetToken();
    }
    
    @Test
    public void createActivityTest() throws Exception{
    	EventCreateData eventCreateData = new EventCreateData("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), new Location("Random Location"), null);
        ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Map<String,String> event = (Map<String, String>) response.getEntity(Object.class);
        String eventId = event.get("eventId");
        System.out.println(eventId);
        Activity activity = new Activity(eventId, "Test create activity", "Test", new Location("Shasta"), null, null);
        ClientResponse response1 = TestHelper.doPOST(endpoint.toString(), "v1/event/"+eventId+"/activity/create", token, activity);
        Activity actresp = response1.getEntity(Activity.class);
        System.out.println(actresp);
    }
    
//  @Test
//  public void createEventTestJSON() throws Exception {
//      EventCreateData eventCreateData = new EventCreateData("MySampleEvent", new GregorianCalendar(),
//              new GregorianCalendar(), new Location("Random Location"), null);
//      ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
//      System.out.println(response);
//  }
}
