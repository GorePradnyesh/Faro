package com.zik.faro.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Item {
    private final String id;     //TODO: change to type Id;
    private String name;
    private String assigneeId; //TODO: change to type Id;
    private int count;
    private Unit unit;
    private ActionStatus status;

    public Item(String id, String name, String assigneeId, int count, Unit unit, ActionStatus status) {
        this.id = id;
        this.name = name;
        this.assigneeId = assigneeId;
        this.count = count;
        this.unit = unit;
        this.status = status;
    }

    @XmlElement
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }
}
