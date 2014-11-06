package com.zik.faro.data;


import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Location {
    public final String locationName;
    public final Location location;

    public Location(String locationName, Location location) {
        this.locationName = locationName;
        this.location = location;
    }

    public Location() {
        this(null, null);
    }
}
