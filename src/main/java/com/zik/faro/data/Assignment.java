package com.zik.faro.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Assignment {
    public final String id;             //TODO: change to type Id;
    public final String ownerId;        //TODO: change to type Id; // Owner is activity or event.
    private final List<Item> items = new ArrayList<>();
    private ActionStatus status;

    public Assignment(final String ownerId){
        this.id = UUID.randomUUID().toString();
        this.ownerId = ownerId;
        this.status = ActionStatus.INCOMPLETE;
    }

    public List<Item> getItems(){
        return this.items;     //TODO: change this to not return the items in the class . clone ?
    }

    public void addItem(Item item){
        items.add(item);
    }
}
