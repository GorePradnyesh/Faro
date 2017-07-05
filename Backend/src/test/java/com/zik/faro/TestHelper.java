package com.zik.faro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Principal;
import java.security.interfaces.RSAPrivateKey;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.google.api.client.util.ArrayMap;
import com.google.common.collect.Maps;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.functional.FunctionalEventTest;
import org.assertj.core.util.Lists;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.junit.Assert;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.zik.faro.api.event.CustomDateSerializer;
import com.zik.faro.data.user.FaroSignupDetails;

public class TestHelper {
    /*Private helper functions*/
    //TODO: use Entity writers to get a cleaner flow, without writing marshal functions.

    private static ObjectMapper mapper = new ObjectMapper();
    private static final String HOSTNAME_PROPERTY = "testHostname";
    private static final String PORT_PROPERTY = "port";
    private static SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1,0,0,null));

    static {
    	simpleModule.addSerializer(Calendar.class, new CustomDateSerializer());
    	mapper.registerModule(simpleModule);
    }

    public static void initializeFirebaseAdminSdk() throws IOException {
        // Initialize the Firebase Admin SDK
        // use the path to serviceAccountKey json
        FileInputStream serviceAccount = new FileInputStream(new File("/Users/gaurav/Projects/Faro/Backend/src/main/resources/faro-56043-firebase-adminsdk-out3y-192c0b32ad.json"));
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                .setDatabaseUrl("https://faro-56043.firebaseio.com/")
                .build();

        FirebaseApp.initializeApp(options);
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
            //hostname = "faro-appserver.appspot.com";
        	hostname = "localhost";
        }
        String port = System.getProperty(PORT_PROPERTY);
        if(port == null){
            //port = "80";
        	port = "8888";
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
    
    public static ClientResponse doPUT(String uri, String path, String authToken,
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
                .put(ClientResponse.class,data);
        Assert.assertNotNull(response);
        return response;
    	
    }
    
    public static ClientResponse doPOST(String uri, String path, String authToken,
    		Object postData, MultivaluedMap<String, String> queryParams) throws IOException{
    	
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
                .queryParams(queryParams)
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

    public static ClientResponse doDELETE(String uri, String path, String authToken) {
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

    public static TestFaroUser createTestFaroUser() throws Exception {
        String newRandomEmail = UUID.randomUUID().toString() + "@gmail.com";
        FaroUser newUser = new FaroUser(newRandomEmail,
                "sachin",
                "ramesh",
                "tendulkar",
                "splitwise",
                null,
                null);

        return createTestFaroUser(new FaroSignupDetails(newUser, "hero123#"));

    }

    // TODO: Complete the creation workflow for facebook test user for unit/functional tests
    public static TestFaroUser createTestFaroFacebookUser() throws Exception {
        String newRandomEmail = UUID.randomUUID().toString() + "@gmail.com";
        FaroUser newUser = new FaroUser(newRandomEmail,
                "sachin",
                "ramesh",
                "tendulkar",
                "splitwise",
                null,
                null);
        /**
         *
         "firebase": {
            "identities": {
                "facebook.com": ["105332620054545"],
                "email": ["jawahar_zzrbfdm_vyas@tfbnw.net"]
            },
            "sign_in_provider": "facebook.com"
         }


         */

        String fbUserId = UUID.randomUUID().toString();

        Map<String, Object> claims = Maps.newHashMap();

        ArrayMap<String, Object> identities = new ArrayMap<>();
        identities.put("facebook.com", Lists.newArrayList(fbUserId));

        ArrayMap<String, Object> firebaseClaims = new ArrayMap<>();
        firebaseClaims.put("sign_in_provider", "facebook.com");
        firebaseClaims.put("identities", identities);

        claims.put("firebase", firebaseClaims);

        Task<String> task = FirebaseAuth.getInstance().createCustomToken(fbUserId);
        Tasks.await(task);

        assertThat(task.isSuccessful()).isTrue();

        String firebaseToken = task.getResult();
        assertThat(firebaseToken).isNotNull();

        return createTestFaroUser(new FaroSignupDetails(newUser, null, firebaseToken));
    }

    public static TestFaroUser createTestFaroUser(FaroSignupDetails faroSignupDetails) throws Exception {
        ClientResponse clientResponse = signupUser(faroSignupDetails);
        String token = clientResponse.getEntity(String.class);

        return new TestFaroUser(faroSignupDetails.getFaroUser(), token);
    }



    public static String createUserAndGetToken() throws Exception{
    	String newRandomEmail = UUID.randomUUID().toString() + "@gmail.com";
        FaroUser newUser = new FaroUser(newRandomEmail,
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
    
    public static String createUserAndGetToken(String email) throws Exception{
    	String newRandomEmail = email;
        FaroUser newUser = new FaroUser(newRandomEmail,
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
        FaroUser newUser = new FaroUser(email,
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

    // Create an event for all activity tests
    public static String createEventForTest(String token, URL endpoint) throws IOException{
        Event eventCreateData = new Event("MySampleEvent", Calendar.getInstance(),
                Calendar.getInstance(), "Description", false, null, new Location("Random Location", null, null), null, null, "Mafia god");
        ClientResponse response = TestHelper.doPOST(endpoint.toString(), "v1/event/create", token, eventCreateData);
        Event event = response.getEntity(Event.class);
        FunctionalEventTest.assertEntity(eventCreateData, event);
        return event.getId();
    }

    public static void deleteEvent(String token, URL endpoint, String eventId) {
        ClientResponse response = doDELETE(endpoint.toString(), "v1/event/{" + eventId + "}", token);
    }
   
    public static void main(String[] args) throws Exception {
		doPOST("", "", "", 123);
	}


}
