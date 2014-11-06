package com.zik.faro.api.responder;

import com.zik.faro.data.DateOffset;
import com.zik.faro.data.Location;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EventCreateData {
    public final String         eventName;
    public final DateOffset     startDate;
    public final DateOffset     endDate;
    public final Location       location;

    public EventCreateData(String eventName, DateOffset startDate, DateOffset endDate, Location location) {
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
    }

    private EventCreateData() {
        // too satisfy JAXB
        this.eventName = null;
        this.startDate = null;
        this.endDate = null;
        this.location = null;
    }
}