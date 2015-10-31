package com.zik.faro.api.unit;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;

import com.zik.faro.persistence.datastore.data.EventDo;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.*;

import javax.xml.bind.JAXBException;
import java.io.IOException;


@Ignore
public class SampleRestUnitTest extends JerseyTest {
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

    //TODO: Use mocking in this case to change the behavior of the methods invoked by the API handlers.
    /*The only behavior we want to test is that the handlers and doing the handling correctly.*/

    public SampleRestUnitTest() throws JAXBException {
        super("com.zik.faro.api");
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
        EventDo testEvent = mapper.readValue(responseMessage, EventDo.class);
        Assert.assertNotNull(testEvent.getEventId());
        Assert.assertNotNull(testEvent.getEventName());
    }


}
