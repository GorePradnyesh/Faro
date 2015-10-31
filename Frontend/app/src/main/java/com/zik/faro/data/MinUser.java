package com.zik.faro.data;

public class MinUser {

    private String firstName;
    private String lastName;
    private String email;
    private String expenseUserId;
    
    public MinUser(final String firstName, final String lastName, final String email){
        this(firstName, lastName, email, null);
    }

    public MinUser(final String firstName, final String lastName,  final String email, final String expenseUserId){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.expenseUserId = expenseUserId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExpenseUserId() {
        return expenseUserId;
    }

    public void setExpenseUserId(String expenseUserId) {
        this.expenseUserId = expenseUserId;
    }
}