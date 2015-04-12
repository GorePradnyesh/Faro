package com.zik.faro.functional;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.zik.faro.TestHelper;
import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.data.DateOffset;
import com.zik.faro.data.Location;

import com.zik.faro.data.user.FaroUser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;


@Ignore
public class FunctionalEventTest {
    private static URL endpoint;

    @BeforeClass
    public static void init() throws MalformedURLException {
        endpoint = TestHelper.getExternalTargetEndpoint();
    }

    @Test
    public void createEventTestJSON() throws Exception {
        EventCreateData eventCreateData = new EventCreateData("MySampleEvent", new DateOffset(new Date(), 0),
                new DateOffset(new Date(), 60 * 1000), new Location("Random Location"), null);
        String body = TestHelper.getJsonRep(eventCreateData);
        createEventTest(body, MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void createEventTestXML() throws Exception {
        EventCreateData eventCreateData = new EventCreateData("MySampleEvent", new DateOffset(new Date(), 0),
                new DateOffset(new Date(), 60 * 1000), new Location("Random Location"), null);
        String body = TestHelper.getXMLRep(eventCreateData, EventCreateData.class);
        createEventTest(body, MediaType.APPLICATION_XML_TYPE);
    }

    public String createTestUser() throws URISyntaxException, IOException {
        String newRandomEmail = UUID.randomUUID().toString() + "@gmail.com";
        FaroUser newUser = new FaroUser(newRandomEmail,
                "sachin",
                "ramesh",
                "tendulkar",
                "splitwise",
                null,
                null);
        FaroSignupDetails faroSignupDetails = new FaroSignupDetails(newUser, "hero123#");
        ClientResponse clientResponse = TestHelper.signupUser(faroSignupDetails);
        return clientResponse.getEntity(String.class);
    }

    public void createEventTest(String body, MediaType mediaType) throws URISyntaxException, IOException {
        String token = createTestUser();
        Client c = Client.create();
        WebResource webResource = c.resource(endpoint.toURI());
        String response = webResource
                .path("v1")
                .path("event")
                .path("create")
                .header("Content-Type", mediaType)
                .header("authentication", token)
                .post(String.class, body);
        System.out.println(response);
        Assert.assertNotNull(response);
        Assert.assertTrue(!response.isEmpty());
    }

    //@Test
    public void getEventDetailsTest() throws URISyntaxException {
        Client c = Client.create();
        WebResource webResource = c.resource(endpoint.toURI());
        String response = webResource
                .path("v1")
                .path("event")
                .path("myExampleEventID")
                .get(String.class);
        Assert.assertNotNull(response);
        Assert.assertTrue(!response.isEmpty());
    }
}
