package com.zik.faro;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.zik.faro.api.authentication.LoginHandler;
import com.zik.faro.api.authentication.SignupHandler;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.data.user.FaroUser;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Principal;
import java.util.UUID;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class TestHelper {
    /*Private helper functions*/
    //TODO: use Entity writers to get a cleaner flow, without writing marshal functions.

    private static ObjectMapper mapper = new ObjectMapper();
    private static final String HOSTNAME_PROPERTY = "testHostname";
    private static final String PORT_PROPERTY = "port";

    public static String getJsonRep(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    public static String getXMLRep(Object object, Class clazz) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(clazz);
        StringWriter stringWriter = new StringWriter();
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(object, stringWriter);
        String xmlRep = stringWriter.toString();
        return xmlRep;
    }


    public static URL getExternalTargetEndpoint() throws MalformedURLException {
        String hostname = System.getProperty(HOSTNAME_PROPERTY);
        if(hostname == null){
            hostname = "localhost";
        }
        String port = System.getProperty(PORT_PROPERTY);
        if(port == null){
            port = "8080";
        }
        return new URL("http://" + hostname + ":" + port);
    }

    public static ClientResponse login(String username, String password)
            throws URISyntaxException, MalformedURLException {
        Client client = Client.create();
        WebResource webResource = client.resource(getExternalTargetEndpoint().toURI());

        ClientResponse response = webResource
                .path("v1")
                .path("nativeLogin")
                .path("login")
                .queryParam("username", username)
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, password);

        return response;
    }

    public static ClientResponse signupUser(FaroSignupDetails newUserSignupDetails)
            throws URISyntaxException, IOException {
        Client client = Client.create();
        WebResource webResource = client.resource(getExternalTargetEndpoint().toURI());
        String body = TestHelper.getJsonRep(newUserSignupDetails);
        System.out.println("body = " +body);

        ClientResponse response = webResource
                .path("v1")
                .path("nativeLogin")
                .path("signup")
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, body);

        return response;
    }

    private static SecurityContext createMockSecurityContext(final String userId) {
        // Setup mocked security context object
        SecurityContext securityContextMock = mock(SecurityContext.class);
        when(securityContextMock.getUserPrincipal()).thenReturn(new Principal() {
            @Override
            public String getName() {
                return userId;
            }
        });

        return securityContextMock;
    }

    public static SecurityContext setupMockSecurityContext() {
        final String testUserId = "testuser-" + UUID.randomUUID() + "@gmail.com";
        return createMockSecurityContext(testUserId);

    }

    public static SecurityContext setupMockSecurityContext(String userId) {
        return createMockSecurityContext(userId);
    }
}
