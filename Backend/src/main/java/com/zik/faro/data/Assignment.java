package com.zik.faro.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zik.faro.commons.exceptions.IllegalDataOperation;


/**
 * NOTE: This is not an @Entity because Assignment is CONTAINED within Activity for now. This therefore does not
 * need to be a storage @Entity by itself.
*/

@XmlRootElement
public class Assignment {
    private String id;
    private ActionStatus status;
    private Map<String,Item> items = new HashMap<String, Item>();

    private static final String NA = "N/A";
    
    private Assignment(){
    }
    
    public Assignment(final String id, final ActionStatus status) throws IllegalDataOperation{
    	if(id == null || id.isEmpty()){
    		throw new IllegalDataOperation("Assignment id cannot be null");
    	}
    	this.id = id;
    	this.status = status;
    }
    
    @XmlElement
    public List<Item> getItems(){
        return new ArrayList<Item>(this.items.values());     
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
    
    public void setId(String id){
    	this.id = id;
    }
}
