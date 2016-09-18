package com.zik.faro.api.unit;

import java.util.Calendar;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.google.appengine.repackaged.org.apache.http.protocol.HTTP;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.TestHelper;
import com.zik.faro.api.event.EventCreateHandler;
import com.zik.faro.api.event.EventHandler;
import com.zik.faro.data.Event;
import com.zik.faro.data.Location;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.EventUserDo;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

public class EventApiTest {
	private static final LocalServiceTestHelper helper = new LocalServiceTestHelper(
			new LocalDatastoreServiceTestConfig()
					.setDefaultHighRepJobPolicyUnappliedJobPercentage(50));

	private static SecurityContext securityContextMock;

	static {
		ObjectifyService.register(EventDo.class);
		ObjectifyService.register(EventUserDo.class);
		 
	}

	@BeforeClass
	public static void init() {
		ObjectifyService.begin(); // This is needed to set up the ofy service.

		// Create mocked security context for the handler objects
		securityContextMock = TestHelper.setupMockSecurityContext();
	}

	@Before
	public void setUp() {
		helper.setUp();
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Test
	public void testEventCreation() {
		String eventName = UUID.randomUUID().toString();

		Event eventCreateData = new Event(eventName,
				Calendar.getInstance(), Calendar.getInstance(), "Test event description",
				false, null, new Location(
						"Random Location"), null, null,"Mafia god");

		EventCreateHandler eventCreateHandler = new EventCreateHandler();
		Whitebox.setInternalState(eventCreateHandler, securityContextMock);
		Event event = eventCreateHandler
				.createEvent(eventCreateData).getEntity();

		String eventId = event.getId();

		EventHandler eventHandler = new EventHandler();
		Whitebox.setInternalState(eventHandler, securityContextMock);

		event = eventHandler.getEventDetails(eventId).getEntity();
		Assert.assertEquals(eventId, event.getId());
		Assert.assertEquals(eventName, event.getEventName());
	}


	@Test
	public void testEventDeletion() {
		String eventName = UUID.randomUUID().toString();

		Event eventCreateData = new Event(eventName,
				Calendar.getInstance(), Calendar.getInstance(), "Test event description",
				false, null, new Location(
				"Random Location"), null, null,"Mafia god");

		EventCreateHandler eventCreateHandler = new EventCreateHandler();
		Whitebox.setInternalState(eventCreateHandler, securityContextMock);
		Event event = eventCreateHandler
				.createEvent(eventCreateData).getEntity();

		String eventId = event.getId();

		EventHandler eventHandler = new EventHandler();
		Whitebox.setInternalState(eventHandler, securityContextMock);

		event = eventHandler.getEventDetails(eventId).getEntity();
		Assert.assertEquals(eventId, event.getId());
		Assert.assertEquals(eventName, event.getEventName());

		eventHandler.deleteEvent(eventId);
		boolean eventDeleted = false;
		try {
			eventHandler.getEventDetails(eventId).getEntity();
		}catch(WebApplicationException e) {
			eventDeleted = true;
			Assert.assertEquals(NOT_FOUND.getStatusCode(), e.getResponse().getStatus());
		}
		Assert.assertTrue(eventDeleted);
	}

//	@Test
//	public void testEventControlFlag() {
//		String eventName = UUID.randomUUID().toString();
//		EventCreateHandler eventCreateHandler = new EventCreateHandler();
//		Whitebox.setInternalState(eventCreateHandler, securityContextMock);
//		EventHandler eventHandler = new EventHandler();
//		Whitebox.setInternalState(eventHandler, securityContextMock);
//
//		// Store event
//		Event eventCreateData = new Event(eventName,
//				Calendar.getInstance(), Calendar.getInstance(), "Test event description",
//				false, null, new Location(
//						"Random Location"), null, null,"Mafia god");
//		Event minEvent = eventCreateHandler
//				.createEvent(eventCreateData).getEntity();
//
//		String eventId = minEvent.getId();
//		// Retrieve event, verify control flag
//		Event event = eventHandler.getEventDetails(eventId).getEntity();
//		Assert.assertEquals(false, event.getControlFlag());
//
//		// Disable control flag
//		eventHandler.disableEventControl(eventId);
//		
//		// Retrieve event again, verify control flag
//		event = eventHandler.getEventDetails(eventId).getEntity();
//		Assert.assertEquals(true, event.getControlFlag());
//	}
//
//	@Test
//	public void testEventControlFlagBadEventId() {
//		EventHandler eventHandler = new EventHandler();
//		Whitebox.setInternalState(eventHandler, securityContextMock);
//
//		boolean correctExceptionTriggered = false;
//		// Disable control flag
//		try {
//			eventHandler.disableEventControl("BADEVENTID");
//		} catch (WebApplicationException ex) {
//			Assert.assertEquals(NOT_FOUND.getStatusCode(), ex
//					.getResponse().getStatus());
//			correctExceptionTriggered = true;
//		}
//		Assert.assertTrue(correctExceptionTriggered);
//	}
}
