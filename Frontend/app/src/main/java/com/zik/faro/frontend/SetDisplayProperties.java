package com.zik.faro.frontend;

import com.zik.faro.data.Event;
import com.zik.faro.data.EventUser;
import com.zik.faro.data.Poll;
import com.zik.faro.data.user.InviteStatus;

/**
 * Created by nakulshah on 4/26/15.
 */
public class SetDisplayProperties {

    static EventListHandler eventListHandler = EventListHandler.getInstance();

    static public int getEventStatusImage(Event event){
        InviteStatus inviteStatus = eventListHandler.getUserEventStatus(event.getEventId());
        switch (inviteStatus){
            case ACCEPTED:
                return R.drawable.green;
            case MAYBE:
                return R.drawable.yellow;
            case INVITED:
                return R.drawable.red;
            default:
                return R.drawable.red;
        }
    }

    static public int getPollStatusImage(Poll poll){
        switch (poll.getStatus()){
            case OPEN:
                return R.drawable.green;
            case CLOSED:
                return R.drawable.red;
            default:
                return R.drawable.red;
        }
    }
}
