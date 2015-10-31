package com.zik.faro;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Principal;
import java.util.Calendar;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.junit.Assert;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.zik.faro.api.event.CustomDateSerializer;
import com.zik.faro.api.responder.FaroSignupDetails;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class TestHelper {
    /*Private helper functions*/
    //TODO: use Entity writers to get a cleaner flow, without writing marshal functions.

    private static ObjectMapper mapper = new ObjectMapper();
    private static final String HOSTNAME_PROPERTY = "testHostname";
    private static final String PORT_PROPERTY = "port";
    private static SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1,0,0,null));
    static{
    	simpleModule.addSerializer(Calendar.class, new CustomDateSerializer());
    	mapper.registerModule(simpleModule);
    }
    
    
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
        Client client = RestClient.getInstance().getClient();
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
        Client client = RestClient.getInstance().getClient();
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
    
    public static ClientResponse doPOST(String uri, String path, String authToken,
    		Object postData) throws IOException{
    	
    	Client client = RestClient.getInstance().getClient();
        WebResource webResource = client.resource(uri);
        String data;
        if(postData.getClass().equals(String.class)){
        	data = (String) postData;
        }else{
        	data = mapper.writeValueAsString(postData);
        }
         
        System.out.println(data);
        ClientResponse response = webResource
                .path(path)
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .header("authentication", authToken)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class,data);
        Assert.assertNotNull(response);
        return response;
    	
    }
    
    public static ClientResponse doGET(String uri, String path,
    		MultivaluedMap<String, String> queryParams, String authToken) throws IOException{
    	Client client = RestClient.getInstance().getClient();
        WebResource webResource = client.resource(uri);
        ClientResponse response = webResource
                .path(path)
                .queryParams(queryParams)
                .header("Content-Type", MediaType.APPLICATION_JSON_TYPE)
                .header("authentication", authToken)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
        Assert.assertNotNull(response);
        return response;
    	
    }
    
    public static ClientResponse doDELETE(String uri, String path, String authToken){
    	Client client = RestClient.getInstance().getClient();
        WebResource webResource = client.resource(uri);
        ClientResponse response = webResource
                .path(path)
                .header("authentication", authToken)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .delete(ClientResponse.class);
        Assert.assertNotNull(response);
        return response;
    }
    
    public static String createUserAndGetToken() throws Exception{
    	String newRandomEmail = UUID.randomUUID().toString() + "@gmail.com";
        FaroUserDo newUser = new FaroUserDo(newRandomEmail,
                "sachin",
                "ramesh",
                "tendulkar",
                "splitwise",
                null,
                null);
        FaroSignupDetails faroSignupDetails = new FaroSignupDetails(newUser, "hero123#");
        ClientResponse clientResponse = signupUser(faroSignupDetails);
        return clientResponse.getEntity(String.class);
    }
    
    public static String createUser(String email, String fname, String lname, String mname) throws Exception{
    	FaroUserDo newUser = new FaroUserDo(email,
                fname,
                mname,
                lname,
                "splitwise",
                null,
                null);
        FaroSignupDetails faroSignupDetails = new FaroSignupDetails(newUser, "hero123#");
        ClientResponse clientResponse = signupUser(faroSignupDetails);
        Assert.assertNotNull(clientResponse);
        Assert.assertEquals(200, clientResponse.getStatus());
        return email;
    }
   
    public static void main(String[] args) throws Exception {
		doPOST("", "", "", 123);
	}
}
