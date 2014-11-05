package com.zik.faro.data.user;

import javax.xml.bind.annotation.XmlRootElement;

// TODO: Add data validations

@XmlRootElement
public class FaroUser {
    public final String             email;
    public final FaroUserName       userName;
    public final String             externalExpenseID;
    public final String             telephone;   //TODO: type tel number
    public final Address            address;

    public FaroUser(String email,
                FaroUserName userName,
                String externalExpenseID,
                String telephone,
                Address address) {
        this.email = email;

        this.userName = userName;
        this.externalExpenseID = externalExpenseID;
        this.telephone = telephone;
        this.address = address;
    }

    // To Satisfy the JAXB no-arg constructor requirement
    private FaroUser(){
        this(null, null, null, null, null);
    }
}
