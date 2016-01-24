package com.zik.faro.frontend;

public class Location {
    public String locationName;
    public GeoPosition position;

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

    private Location() {
        //this(null, null); // to satisfy JAXB
    }
}
