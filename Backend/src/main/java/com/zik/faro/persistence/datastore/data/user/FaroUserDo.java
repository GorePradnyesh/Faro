package com.zik.faro.persistence.datastore.data.user;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.appengine.repackaged.com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;
import com.zik.faro.data.IllegalDataOperation;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.AppInviteStatus;
import com.zik.faro.persistence.datastore.data.BaseEntityDo;

@Entity
@XmlRootElement
public class FaroUserDo extends BaseEntityDo{
    @Index
    private String             firstName;
    private String             middleName;
    private String             lastName;
    private String             externalExpenseID;
    private String             telephone;   
    private Address 			address;
    private AppInviteStatus 	inviteStatus = AppInviteStatus.INVITED;
    private List<String> 		tokens = new ArrayList<String>();
    private String 				userTopic;

    // To be used for creating users who are invited by other faro users
    // Due to lack of info available, using only email
    // Invite Status to be updated later when user clicks on email, downloads app and signs up with more details
    public FaroUserDo(final String email) {
        this(email, null, null, null, null, null, null, AppInviteStatus.INVITED, null, null);
    }
    
    public FaroUserDo(final String email,
            final String firstName,
            final String middleName,
            final String lastName,
            final String externalExpenseID,
            final String telephone,
            final Address address,
            final AppInviteStatus inviteStatus,
            final List<String> tokens,
            final String userTopic) {
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
        this.inviteStatus = inviteStatus;
        this.tokens = tokens;
        this.userTopic = userTopic;
    }

    public FaroUserDo(final String email,
                      final String firstName,
                      final String middleName,
                      final String lastName,
                      final String externalExpenseID,
                      final String telephone,
                      final Address address) {
        this(email, firstName, middleName, lastName, externalExpenseID, telephone, address,
        		AppInviteStatus.ACCEPTED, null, null);
    }

    public void setIfNullExternalExpenseID(final String externalExpenseID) throws IllegalDataOperation {
        if(this.externalExpenseID == null) {
            this.externalExpenseID = externalExpenseID;
        }else{
            throw new IllegalDataOperation("Attempting to set a nonNull externalExpenseID");
        }
    }

    // To Satisfy the JAXB no-arg constructor requirement
    public FaroUserDo(){

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
