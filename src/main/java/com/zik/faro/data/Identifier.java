package com.zik.faro.data;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

@XmlRootElement
public class Identifier {
    private String idString;

    public Identifier(){
        this.idString = UUID.randomUUID().toString();
    }

    private Identifier(final String idString){
        this.idString = idString;
    }

    public Identifier fromString(final String idString) {
        return new Identifier(idString);
    }

    @XmlElement
    public String getIdString(){
        return this.idString;
    }
}
