package com.zik.faro.frontend;

import com.zik.faro.data.Event;
import com.zik.faro.data.EventUser;
import com.zik.faro.data.Poll;
import com.zik.faro.data.user.EventInviteStatus;

public class SetDisplayProperties {

    static public int getEventStatusImage(EventInviteStatus eventInviteStatus){
        switch (eventInviteStatus){
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
