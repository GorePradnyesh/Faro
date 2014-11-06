package com.zik.faro.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GeoPosition {
    public final double latitude;
    // TODO: What about N/S
    public final double longitude;
    // TODO: What about E/W

    // TODO: double accuracy?
    // TODO: altitude; ?

    public GeoPosition(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GeoPosition() {
        this(0.0, 0.0);
    }

}
