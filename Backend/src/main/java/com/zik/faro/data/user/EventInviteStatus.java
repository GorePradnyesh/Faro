package com.zik.faro.data.user;

public enum EventInviteStatus {
    ACCEPTED, INVITED, MAYBE, DECLINED;

    public static EventInviteStatus fromValue(String inviteStatus) {
        switch (inviteStatus) {
            case "ACCEPTED" : return ACCEPTED;
            case "INVITED" : return INVITED;
            case "MAYBE" : return MAYBE;
            case "DECLINED" : return DECLINED;
            default: throw new IllegalArgumentException("Invalid status string provided to get an instance of EventInviteStatus enum");
        }
    }
}
