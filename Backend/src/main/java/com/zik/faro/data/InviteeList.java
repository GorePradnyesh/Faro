package com.zik.faro.data;

import java.util.HashMap;
import java.util.Map;

import com.zik.faro.data.user.EventInviteStatus;

public class InviteeList {
	
	// TODO: Do we need the eventId? In the service call client already provides the eventId
    private Map<String, Invitees> userStatusMap = new HashMap<String,Invitees>();

    public InviteeList(){
        
    }


    public void addUserStatus(final MinUser minUser, final EventInviteStatus status){
        this.userStatusMap.put(minUser.getEmail(), new Invitees(minUser.getFirstName(), minUser.getLastName(), minUser.getEmail(), minUser.getExpenseUserId(), status));
    }
    
    public Map<String,Invitees> getUserStatusMap(){
    	return this.userStatusMap;
    }
    
    public void setUserStatusMap(Map<String,Invitees> userStatusMap){
    	this.userStatusMap = userStatusMap;
    }

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

		public EventInviteStatus getInviteStatus() {
			return inviteStatus;
		}

		public void setInviteStatus(EventInviteStatus inviteStatus) {
			this.inviteStatus = inviteStatus;
		}

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

		private String firstName;
    	private String lastName;
        private String email;
    	private String expenseId;
    	private EventInviteStatus inviteStatus;
    	private Invitees(){}
    	
    	public Invitees(final String firstName, final String lastName, final String email, final String expenseId, final EventInviteStatus inviteStatus){
    		this.firstName = firstName;
    		this.lastName = lastName;
            this.email = email;
    		this.expenseId = expenseId;
    		this.inviteStatus = inviteStatus;
    	}
    }
    
}
