package com.zik.faro.frontend;

import com.zik.faro.data.Location;

/**
 * Created by nakulshah on 4/2/17.
 */

public class GetLocationAddressString {
    public static String getLocationAddressString(Location location){
        if (location.getLocationName() != null){
            return location.getLocationName();
        }else if (location.getLocationAddress() != null) {
            return location.getLocationAddress();
        }else{
            return "Event location";
        }
    }
}
