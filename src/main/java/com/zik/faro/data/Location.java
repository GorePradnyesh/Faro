package com.zik.faro.data;


import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Location {
    public final String locationName;
    public final GeoPosition position;

    public Location(String locationName, GeoPosition position) {
        this.locationName = locationName;
        this.position = position;
    }

    public Location() {
        this(null, null);
    }
}
