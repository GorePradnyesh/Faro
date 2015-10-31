package com.zik.faro.frontend.data;

public class Location {
    public final String locationName;
    public final GeoPosition position;

    public Location(String locationName, GeoPosition position) {
        this.locationName = locationName;
        this.position = position;
    }

    public Location(String locationName){
        this.locationName = locationName;
        this.position = null;
    }

    public Location(GeoPosition position){
        this.position = position;
        this.locationName = null;
    }

}
