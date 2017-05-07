package com.zik.faro.persistence.datastore;

import java.util.GregorianCalendar;

import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Location;
import com.zik.faro.data.GeoPosition;


public class DatastoreObjectifyTransactionDALTest {
	
	public void testUpdateActivity(){
		// Create activity and store it.
		GeoPosition geoPosition = new GeoPosition(0,0);
		Activity a = new Activity("testEvent", "test", "test",
				new Location("Home", "667 Encore Way", geoPosition), new GregorianCalendar(), new Assignment());
		DatastoreObjectifyDAL.storeObject(a);
	}
}
