package com.zik.faro.frontend.data.user;

public class FaroUser {
    private String             email;
    private String             firstName;
    private String             middleName;
    private String             lastName;
    private String             externalExpenseID;
    private String             telephone;   //TODO: type tel number
    private Address            address;


    public FaroUser(final String email){
        this(email, null, null, null, null, null, null);
    }

    public FaroUser(final String email,
                final String firstName,
                final String middleName,
                final String lastName,
                final String externalExpenseID,
                final String telephone,
                final Address address) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.externalExpenseID = externalExpenseID;
        this.telephone = telephone;
        this.address = address;
    }

    public void setIfNullExternalExpenseID(final String externalExpenseID){
        if(this.externalExpenseID == null) {
            this.externalExpenseID = externalExpenseID;
        }else{
            throw new RuntimeException("Attempting to set a nonNull externalExpenseID");
        }
    }

    public String getId() { return this.email; }

    public String getEmail() {
        return email;
    }

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
}
