package com.zik.faro.data.user;

import com.google.common.base.MoreObjects;

import com.google.common.base.Strings;
import com.zik.faro.data.IllegalDataOperation;

public class FaroUser {
    private String             email;
    private String             firstName;
    private String             middleName;
    private String             lastName;
    private String             externalExpenseID;
    private String             telephone;
    private Address            address;
    private AppInviteStatus    inviteStatus = AppInviteStatus.INVITED;
    private LargeProfileImage  largeProfileImage;
    private SmallProfileImage  smallProfileImage;

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
        // Ensure Email is a valid value as it is a mandatory field
        if (Strings.isNullOrEmpty(email)) {
            throw new IllegalArgumentException("Email is null/empty");
        }

        this.email = email;
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
        if (this.externalExpenseID == null) {
            this.externalExpenseID = externalExpenseID;
        } else{
            throw new IllegalDataOperation("Attempting to set a nonNull externalExpenseID");
        }
    }

    // To Satisfy the JAXB no-arg constructor requirement
    public FaroUser(){

    }

    /*Getters*/

    public String getEmail() { return this.email; }

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

    public void setEmail(String email) {
        this.email = email;
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

	public AppInviteStatus getInviteStatus() {
		return inviteStatus;
	}

	public void setInviteStatus(AppInviteStatus inviteStatus) {
		this.inviteStatus = inviteStatus;
	}

    public LargeProfileImage getLargeProfileImage() {
        return largeProfileImage;
    }

    public void setLargeProfileImage(LargeProfileImage largeProfileImage) {
        this.largeProfileImage = largeProfileImage;
    }

    public SmallProfileImage getSmallProfileImage() {
        return smallProfileImage;
    }

    public void setSmallProfileImage(SmallProfileImage smallProfileImage) {
        this.smallProfileImage = smallProfileImage;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("email", getEmail())
                .add("firstName", getFirstName())
                .add("middleName", getMiddleName())
                .add("lastName", getLastName())
                .add("externalExpenseID", getExternalExpenseID())
                .add("telephone", getTelephone())
                .add("address", getAddress())
                .add("profileImageUrl", getLargeProfileImage())
                .add("thumbProfileImageUrl", getSmallProfileImage())
                .toString();
    }
}

