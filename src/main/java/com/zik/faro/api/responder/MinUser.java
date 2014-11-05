package com.zik.faro.api.responder;

import com.zik.faro.data.user.FaroUserName;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MinUser {
    public final FaroUserName userName;
    public final String email;     //TODO: Make of type email ?
    public final String expenseUserId;

    private MinUser(){          // For JAXB
        this(null, null);
    }

    public MinUser(final FaroUserName userName, final String email){
        this.userName = userName;
        this.email = email;
        this.expenseUserId = null;
    }

    public MinUser(final FaroUserName userName, final String email, final String expenseUserId){
        this.userName = userName;
        this.email = email;
        this.expenseUserId = expenseUserId;
    }
}


