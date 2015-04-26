package com.zik.faro.frontend.data.user;

public class Address {
    public final int addressNumber;
    public final String line1;
    public final String line2;
    public final String State;
    public final int zip;

    public Address(int addressNumber, String line2, String line21, String state, int zip) {
        this.addressNumber = addressNumber;
        this.line1 = line2;
        this.line2 = line21;
        State = state;
        this.zip = zip;
    }

    private Address(){
        this(0, null, null, null, 0);
    }
}