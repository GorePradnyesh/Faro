package com.zik.faro.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//TODO:perform data validations

/**
 * NOTE: This is not an @Entity because Assignment is CONTAINED within Activity for now. This therefore does not
 * need to be a storage @Entity by itself.
*/

@XmlRootElement
public class Assignment {
    public String id = null;

    private ActionStatus status = null;
    private Map<String,Item> items = null;

    private static final String NA = "N/A";
    
	public Assignment(){
        this.id = UUID.randomUUID().toString();
        this.status = ActionStatus.INCOMPLETE;
        this.items  = new HashMap<String,Item>();
    }

    @XmlElement
    public List<Item> getItems(){
        return new ArrayList<Item>(this.items.values());     //TODO: change this to not return the items in the class . clone ?
    }

    public void addItem(Item item){
        items.put(item.getId(), item);
    }
    
    public void setItems(Map<String,Item> items){
    	this.items = items;
    }
    
    public Item getItem(String key){
    	return items.get(key);
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }
    
    public String getId(){
    	return this.id;
    }
}
