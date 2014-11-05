package com.zik.faro.data.user;

public class FaroUserName {
    public final String firstName;
    public final String middleName;
    public final String lastName;
    public final String prefix;
    public final String suffix;

    public FaroUserName(String firstName, String middleName, String lastName, String prefix, String suffix) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public FaroUserName(String firstName, String middleName, String lastName) {
        this(firstName, middleName, lastName, "", "");
    }

    public FaroUserName(String firstName, String lastName) {
        this(firstName, "", lastName, "", "");
    }

}
