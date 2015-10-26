package data;

import data.user.FaroUser;

public class SignupDetails {
    private FaroUser faroUser;
    private String password;

    public SignupDetails(FaroUser faroUser, String password) {
        this.faroUser = faroUser;
        this.password = password;
    }

    private SignupDetails() {}

    public FaroUser getFaroUser() {
        return faroUser;
    }

    public String getPassword() {
        return password;
    }
}