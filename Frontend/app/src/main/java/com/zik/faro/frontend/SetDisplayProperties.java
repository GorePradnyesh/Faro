package com.zik.faro.frontend;

/**
 * Created by nakulshah on 4/26/15.
 */
public class SetDisplayProperties {

    static public int getEventStatusImage(Event event){
        switch (event.getEventStatus()) {
            case ACCEPTED:
                return R.drawable.green;
            case MAYBE:
                return R.drawable.yellow;
            case NOTRESPONDED:
                return R.drawable.red;
            default:
                return R.drawable.red;
        }
    }
}
