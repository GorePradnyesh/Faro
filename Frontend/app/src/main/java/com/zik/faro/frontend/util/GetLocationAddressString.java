package com.zik.faro.frontend.util;

import com.zik.faro.data.Location;

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
