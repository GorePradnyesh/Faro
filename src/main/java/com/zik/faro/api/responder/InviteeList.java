package com.zik.faro.api.responder;

import com.zik.faro.data.user.InviteStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class InviteeList {
    public final String eventID;
    public final Map<MinUser, InviteStatus> userStatusMap = new HashMap<>();

    public InviteeList(final String eventID){
        this.eventID = eventID;
    }

    private InviteeList(){
        this(null);
    }

    public void addUserStatus(final MinUser minUser, final InviteStatus status){
        this.userStatusMap.put(minUser, status);
    }
}
