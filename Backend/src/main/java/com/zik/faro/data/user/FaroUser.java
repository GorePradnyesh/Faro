package com.zik.faro.data.user;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.base.Strings;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.zik.faro.commons.exceptions.IllegalDataOperation;

@Entity
@XmlRootElement
public class FaroUser {
    @Id @Index
    private String             email;
    @Index
    private String             firstName;
    private String             middleName;
    private String             lastName;
    private String             externalExpenseID;
    private String             telephone;   
    private Address            address;

    public FaroUser(final String email) {
        this(email, null, null, null, null, null, null);
    }

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
    }

    public void setIfNullExternalExpenseID(final String externalExpenseID) throws IllegalDataOperation {
        if(this.externalExpenseID == null) {
            this.externalExpenseID = externalExpenseID;
        }else{
            throw new IllegalDataOperation("Attempting to set a nonNull externalExpenseID");
        }
    }

    // To Satisfy the JAXB no-arg constructor requirement
    private FaroUser(){

    }

    /*Getters*/
    
    public String getEmail() { return this.email; }

//    public String getEmail() {
//        return email;
//    }

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

   @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
