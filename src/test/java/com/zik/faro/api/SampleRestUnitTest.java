package com.zik.faro.api;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;

import com.zik.faro.data.Event;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;


public class SampleRestUnitTest extends JerseyTest {
    /*
    NOTE: Because the Grizzly framework does not launch the application as war, the web.xml does not get read.
    This is why the servlet mapping that is present in the web.xml will not be applied.
    <servlet-mapping>
        <servlet-name>Jersey Web Application</servlet-name>
        <url-pattern>/v1/*</url-pattern>
    </servlet-mapping>

    Therefor currently the /v1/ path is omitted from the unit tests. Please bear this inconsistency in mind while
    writing actual production client code against the service
    TODO: Add a cleaner workaround for this problem.
     */

    public SampleRestUnitTest(){
        super("com.zik.faro.api");
    }

    @Ignore
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
}
