package com.zik.faro.data;

public class Item {
   
	private String id;
    private String name;
    private String assigneeId;
    private int count;
    private Unit unit;
    private ActionStatus status;

    private Item(){ // To satisfy JAXB
    }
    
    
    public Item(String name, String assigneeId, int count, Unit unit, String id) throws IllegalDataOperation {
    	this.id = id;
        this.name = name;
        this.assigneeId = assigneeId;
        this.count = count;
        this.unit = unit;
        this.status = ActionStatus.INCOMPLETE;
    }

    public Item(String name, String assigneeId, int count, Unit unit) throws IllegalDataOperation {
        this(name,assigneeId,count,unit,null);
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
		this.id = id;
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
