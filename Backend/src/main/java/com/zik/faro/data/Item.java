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
    
    
    public Item(String name, String assigneeId, int count, Unit unit, String id) {
    	this.id = id;
        this.name = name;
        this.assigneeId = assigneeId;
        this.count = count;
        this.unit = unit;
        this.status = ActionStatus.INCOMPLETE;
    }

    public Item(String name, String assigneeId, int count, Unit unit) {
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
    

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
