package com.zik.faro.api.responder;

import com.zik.faro.data.user.FaroUserName;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class MinUser {
    public final FaroUserName userName;
    public final String email;     //TODO: Make of type email ?

    private MinUser(){          // For JAXB
        this(null, null);
    }

    public MinUser(final FaroUserName userName, final String email){
        this.userName = userName;
        this.email = email;
    }
}


