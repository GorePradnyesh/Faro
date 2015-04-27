package com.zik.faro.functional;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.GregorianCalendar;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.zik.faro.TestHelper;
import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.data.Location;


@Ignore
public class FunctionalEventTest {

    private static URL endpoint;

    @BeforeClass
    public static void init() throws MalformedURLException {
        endpoint = TestHelper.getExternalTargetEndpoint();
    }

    @Test
    public void createEventTestJSON() throws IOException, URISyntaxException {
        EventCreateData eventCreateData = new EventCreateData("MySampleEvent", new GregorianCalendar(),
        		new GregorianCalendar(), new Location("Random Location"), null);
        String body = TestHelper.getJsonRep(eventCreateData);
        createEventTest(body, MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void createEventTestXML() throws JAXBException, URISyntaxException {
        EventCreateData eventCreateData = new EventCreateData("MySampleEvent", new GregorianCalendar(),
        		new GregorianCalendar(), new Location("Random Location"), null);
        String body = TestHelper.getXMLRep(eventCreateData, EventCreateData.class);
        createEventTest(body, MediaType.APPLICATION_XML_TYPE);
    }


    public void createEventTest(String body, MediaType mediaType) throws URISyntaxException {
        Client c = Client.create();
        WebResource webResource = c.resource(endpoint.toURI());
        String response = webResource
                .path("v1")
                .path("event")
                .path("create")
                .queryParam("Signature", "dummySignature")
                .header("Content-Type", mediaType)
                .post(String.class, body);
        System.out.println(response);
        Assert.assertNotNull(response);
        Assert.assertTrue(!response.isEmpty());
    }

    @Test
    public void getEventDetailsTest() throws URISyntaxException {
        Client c = Client.create();
        WebResource webResource = c.resource(endpoint.toURI());
        String response = webResource
                .path("v1")
                .path("event")
                .path("myExampleEventID")
                .queryParam("Signature", "dummySignature")
                .get(String.class);
        Assert.assertNotNull(response);
        Assert.assertTrue(!response.isEmpty());
    }
}
