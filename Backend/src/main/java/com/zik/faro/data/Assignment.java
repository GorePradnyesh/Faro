package com.zik.faro.data;

import java.util.*;


/**
 * NOTE: This is not an @Entity because Assignment is CONTAINED within Activity for now. This therefore does not
 * need to be a storage @Entity by itself.
*/

public class Assignment {
    private String id;
    private ActionStatus status;

    //private Map<String,Item> items = new HashMap<String, Item>();
    private List<Item> items = new ArrayList<>();

    private static final String NA = "N/A";
    
    public Assignment(){
        this.id = UUID.randomUUID().toString();
        this.status = ActionStatus.INCOMPLETE;
    }
    
    public Assignment(final String id, final ActionStatus status){
    	this.id = id;
    	this.status = status;
    }

    public Assignment(final  ActionStatus status){
        this.status = status;
    }
    
    /*public Map<String,Item> getItems(){
    	return this.items;
    }*/

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
    
    /*public List<Item> getItemsList(){
        return new ArrayList<Item>(this.items.values());
    }*/

    public void addItem(Item item){
        //items.put(item.getId(), item);
        items.add(item);
    }
    
    /*public void setItems(Map<String,Item> items){
    	this.items = items;
    }*/
    
    /*public Item getItem(String key){
    	return items.get(key);
    }*/

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
