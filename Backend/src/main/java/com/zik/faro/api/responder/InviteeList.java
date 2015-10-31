package com.zik.faro.api.responder;

import com.zik.faro.data.MinUser;
import com.zik.faro.data.user.InviteStatus;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class InviteeList {
	
	// TODO: Do we need the eventId? In the service call client already provides the eventId
    private Map<String, Invitees> userStatusMap = new HashMap<String,Invitees>();

    public InviteeList(){
        
    }


    public void addUserStatus(final MinUser minUser, final InviteStatus status){
        this.userStatusMap.put(minUser.getEmail(), new Invitees(minUser.getFirstName(), minUser.getLastName(), minUser.getExpenseUserId(), status));
    }
    
    public Map<String,Invitees> getUserStatusMap(){
    	return this.userStatusMap;
    }
    
    public void setUserStatusMap(Map<String,Invitees> userStatusMap){
    	this.userStatusMap = userStatusMap;
    }

    @XmlRootElement
    public static class Invitees {
    	public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public String getExpenseId() {
			return expenseId;
		}

		public void setExpenseId(String expenseId) {
			this.expenseId = expenseId;
		}

		public InviteStatus getInviteStatus() {
			return inviteStatus;
		}

		public void setInviteStatus(InviteStatus inviteStatus) {
			this.inviteStatus = inviteStatus;
		}

		private String firstName;
    	private String lastName;
    	private String expenseId;
    	private InviteStatus inviteStatus;
    	private Invitees(){}
    	
    	public Invitees(final String firstName, final String lastName, final String expenseId, final InviteStatus inviteStatus){
    		this.firstName = firstName;
    		this.lastName = lastName;
    		this.expenseId = expenseId;
    		this.inviteStatus = inviteStatus;
    	}
    }
    
}
