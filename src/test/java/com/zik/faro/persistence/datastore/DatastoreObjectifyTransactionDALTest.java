package com.zik.faro.persistence.datastore;

import java.util.GregorianCalendar;

import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Location;


public class DatastoreObjectifyTransactionDALTest {
	
	public void testUpdateActivity(){
		// Create activity and store it.
		Activity a = new Activity("testEvent", "test", "test",
				new Location("San Jose"), new GregorianCalendar(), new Assignment());
		DatastoreObjectifyDAL.storeObject(a);
	}
}
