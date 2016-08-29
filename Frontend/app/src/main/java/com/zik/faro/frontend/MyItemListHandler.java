package com.zik.faro.frontend;

import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Item;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.util.List;

public class MyItemListHandler {
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    FaroUserContext faroUserContext = FaroUserContext.getInstance();

    /*
     *This is a Singleton class
     */
    private static MyItemListHandler myItemListHandler = null;

    public static MyItemListHandler getInstance(){
        if (myItemListHandler != null){
            return myItemListHandler;
        }
        synchronized (MyItemListHandler.class)
        {
            if(myItemListHandler == null) {
                myItemListHandler = new MyItemListHandler();
            }
            return myItemListHandler;
        }
    }

    private MyItemListHandler(){}

    public ItemsAdapter myItemsAdapter;

    public void removeMyItemFromList(String itemID) {
        for (int i = 0; i < myItemsAdapter.list.size(); i++) {
            Item item = myItemsAdapter.list.get(i);
            if (item.getId().equals(itemID)) {
                myItemsAdapter.list.remove(item);
            }
        }
    }

    public void addMyItemToList(Item item) {
        removeMyItemFromList(item.getId());
        if (item.getStatus().equals(ActionStatus.COMPLETE)){
            myItemsAdapter.insertAtEnd(item);
        }else {
            myItemsAdapter.insertAtBeginning(item);
        }
    }

    public void clearMyItemsList(){
        if (myItemsAdapter != null) {
            myItemsAdapter.list.clear();
        }
    }
}
