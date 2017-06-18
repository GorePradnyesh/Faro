package com.zik.faro.data.user;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.zik.faro.data.BaseEntity;
import com.zik.faro.data.IllegalDataOperation;

public class FaroUser extends BaseEntity{
    private String             	firstName;
    private String             	middleName;
    private String             	lastName;
    private String             	externalExpenseID;
    private String          	telephone;
    private Address 			address;
    private AppInviteStatus 	inviteStatus = AppInviteStatus.INVITED; 
    // List(because user can have multiple devices) of all registration tokens for a user for notification
    private List<String> 		tokens = new ArrayList<String>();
    // User's private topic to be used to multicast notifications to all his devices
    private String 				userTopic;

// Ideally this should be utilized on server side only. Commenting out until absolutely required
// Assumption is user will be forced to give basic details along with email
//    public FaroUser(final String email) {
//        this(email, null, null, null, null, null, null);
//    }

    public FaroUser(final String email,
                      final String firstName,
                      final String middleName,
                      final String lastName,
                      final String externalExpenseID,
                      final String telephone,
                      final Address address) {
        super(email,1L);
    	// Ensure Email is a valid value as it is a mandatory field
        if (Strings.isNullOrEmpty(email)) {
            throw new IllegalArgumentException("Email is null/empty");
        }

        
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.externalExpenseID = externalExpenseID;
        this.telephone = telephone;
        this.address = address;
        // Cannot imagine use case on client side where this should be "INVITED"
        // Primarily for server side to handle "addFriend/inviteFriend" when "friend" is not a farouser 
        this.inviteStatus = AppInviteStatus.ACCEPTED;
    }

    public void setIfNullExternalExpenseID(final String externalExpenseID) throws IllegalDataOperation {
        if(this.externalExpenseID == null) {
            this.externalExpenseID = externalExpenseID;
        }else{
            throw new IllegalDataOperation("Attempting to set a nonNull externalExpenseID");
        }
    }

    // To Satisfy the JAXB no-arg constructor requirement
    public FaroUser(){

    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getExternalExpenseID() {
        return externalExpenseID;
    }

    public String getTelephone() {
        return telephone;
    }

    public Address getAddress() {
        return address;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setExternalExpenseID(String externalExpenseID) {
        this.externalExpenseID = externalExpenseID;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("email", getId())
                .add("firstName", getFirstName())
                .add("middleName", getMiddleName())
                .add("lastName", getLastName())
                .add("externalExpenseID", getExternalExpenseID())
                .add("telephone", getTelephone())
                .add("address", getAddress())
                .toString();
    }

	public AppInviteStatus getInviteStatus() {
		return inviteStatus;
	}

	public void setInviteStatus(AppInviteStatus inviteStatus) {
		this.inviteStatus = inviteStatus;
	}

	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}
	
	public void addToken(String token){
		this.tokens.add(token);
	}

	public String getUserTopic() {
		return userTopic;
	}

	public void setUserTopic(String userTopic) {
		this.userTopic = userTopic;
	}
}
