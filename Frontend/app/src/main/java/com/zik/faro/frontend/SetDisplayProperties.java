package com.zik.faro.frontend;

import com.zik.faro.data.Event;
import com.zik.faro.data.EventUser;
import com.zik.faro.data.Poll;

/**
 * Created by nakulshah on 4/26/15.
 */
public class SetDisplayProperties {

    static EventListHandler eventListHandler = EventListHandler.getInstance();

    static public int getEventStatusImage(Event event){
        EventUser eventUser = eventListHandler.getEventUser(event.getEventId(),
                eventListHandler.getMyUserId());
        switch (eventUser.getInviteStatus()){
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
