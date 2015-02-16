package com.zik.faro.api.responder;


import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MinUser {
    public final String firstName;
    public final String lastName;
    public final String email;     //TODO: Make of type email ?
    public final String expenseUserId;

    private MinUser(){          // For JAXB
        this(null, null, null, null);
    }

    public MinUser(final String firstName, final String lastName, final String email){
        this(firstName, lastName, email, null);
    }

    public MinUser(final String firstName, final String lastName,  final String email, final String expenseUserId){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.expenseUserId = expenseUserId;
    }
}


