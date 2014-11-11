package com.zik.faro.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@XmlRootElement
public class Assignment {
    public final String id;
    public final String eventId;            //TODO: change to type Id;
    public final String activityId;         //TODO: change to type Id; // Owner is activity or event.

    private ActionStatus status;
    private final List<Item> items = new ArrayList<>();

    private static final String NA = "N/A";

    private Assignment(){   // TO satisfy jaxb
        this.eventId = null;
        this.activityId = null;
        this.id = null;
        this.status = null;
    }

    public Assignment(String eventId){
        this.eventId = eventId;
        this.activityId = NA;
        this.id = Identifier.createUniqueIdentifierString();
        this.status = ActionStatus.INCOMPLETE;
    }

    public Assignment(String eventId, String activityId){
        this.eventId = eventId;
        this.activityId = activityId;
        this.id = Identifier.createUniqueIdentifierString();
        this.status = ActionStatus.INCOMPLETE;
    }

    @XmlElement
    public List<Item> getItems(){
        return this.items;     //TODO: change this to not return the items in the class . clone ?
    }

    public void addItem(Item item){
        items.add(item);
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }
}
