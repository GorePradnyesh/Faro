package com.zik.faro.api;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.repackaged.org.apache.http.entity.StringEntity;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;

import com.zik.faro.api.responder.EventCreateData;
import com.zik.faro.data.DateOffset;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;



public class SampleRestUnitTest extends JerseyTest {

    private ObjectMapper mapper;

    /*
    NOTE: Because the Grizzly framework does not launch the application as war, the web.xml does not get read.
    This is why the servlet mapping that is present in the web.xml will not be applied.
    <servlet-mapping>
        <servlet-firstName>Jersey Web Application</servlet-firstName>
        <url-pattern>/v1/*</url-pattern>
    </servlet-mapping>

    Therefor currently the /v1/ path is omitted from the unit tests. Please bear this inconsistency in mind while
    writing actual production client code against the service
    TODO: Add a cleaner workaround for this problem. GIT ISSUE #3
     */

    public SampleRestUnitTest() throws JAXBException {
        super("com.zik.faro.api");
        this.mapper = new ObjectMapper();
    }

    @Test
    public void sampleTest() throws JAXBException, IOException {
        WebResource webResource = resource();
        String responseMessage = webResource/*.path("v1")*/
                .path("event")
                .path("eventID1")
                .queryParam("Signature", "dummySignature").get(String.class);
        Assert.assertNotNull(responseMessage);

        ObjectMapper mapper = new ObjectMapper();
        Event testEvent = mapper.readValue(responseMessage, Event.class);
        Assert.assertNotNull(testEvent.getEventId());
        Assert.assertNotNull(testEvent.getEventName());
    }

    @Test
    public void createEventTestJSON() throws IOException {
        EventCreateData eventCreateData = new EventCreateData("MySampleEvent", new DateOffset(new Date(), 0),
                new DateOffset(new Date(), 60 * 1000), new Location("Random Location"));
        String body = getJsonRep(eventCreateData);
        createEventTest(body, MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void createEventTestXML() throws JAXBException {
        EventCreateData eventCreateData = new EventCreateData("MySampleEvent", new DateOffset(new Date(), 0),
                new DateOffset(new Date(), 60 * 1000), new Location("Random Location"));
        String body = getXMLRep(eventCreateData, EventCreateData.class);
        createEventTest(body, MediaType.APPLICATION_XML_TYPE);
    }

    public void createEventTest(String body, MediaType mediaType){
        WebResource webResource = resource();
        String response = webResource/*.path("v1")*/
                .path("event")
                .path("create")
                .queryParam("Signature", "dummySignature")
                .header("Content-Type", mediaType)
                .post(String.class, body);
        Assert.assertNotNull(response);
        Assert.assertTrue(!response.isEmpty());
    }

    /*Private helper functions*/
    //TODO: use Entity writers to get a cleaner flow, without writing marshal functions.

    private String getJsonRep(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    private String getXMLRep(Object object, Class clazz) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(clazz);
        StringWriter stringWriter = new StringWriter();
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(object, stringWriter);
        String xmlRep = stringWriter.toString();
        return xmlRep;
    }
}
