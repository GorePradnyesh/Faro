package com.zik.faro.frontend.data;


import java.util.UUID;

/**
 * The purpose of this class is to provide encapsulation of data across requests
 * and to provide an abstraction for generating unique identifier strings
 */


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

    public String getIdString(){
        return this.idString;
    }

    public static String createUniqueIdentifierString(){
        return UUID.randomUUID().toString();
    }
}
