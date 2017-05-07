package com.zik.faro.data;



public class Location {


    private String locationName;
    private String locationAddress;
    private GeoPosition position;

    public Location(String locationName, String locationAddress, GeoPosition position) {
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.position = position;
    }


    public Location() {
        //this(null, null); // to satisfy JAXB
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public GeoPosition getPosition() {
        return position;
    }

    public void setPosition(GeoPosition position) {
        this.position = position;
    }
}
