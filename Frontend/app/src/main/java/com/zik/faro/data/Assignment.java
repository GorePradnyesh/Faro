package com.zik.faro.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//TODO:perform data validations

/**
 * NOTE: This is not an @Entity because Assignment is CONTAINED within Activity for now. This therefore does not
 * need to be a storage @Entity by itself.
*/

public class Assignment {
    public final String id;

    private ActionStatus status;
    private final List<Item> items = new ArrayList<>();

    private static final String NA = "N/A";

    public Assignment(){
        this.id = UUID.randomUUID().toString();
        this.status = ActionStatus.INCOMPLETE;
    }


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
