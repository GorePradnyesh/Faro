package com.zik.faro.data.user;

public class Address {
    private int addressNumber;
    private String line1;
    public String line2;
    public String state;
    public int zip;

    public Address(int addressNumber, String line2, String line21, String state, int zip) {
        this.addressNumber = addressNumber;
        this.line1 = line2;
        this.line2 = line21;
        this.state = state;
        this.zip = zip;
    }

    private Address(){
    }
}