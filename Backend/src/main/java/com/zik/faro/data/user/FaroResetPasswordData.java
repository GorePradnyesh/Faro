package com.zik.faro.data.user;

/**
 * Created by granganathan on 5/3/15.
 */
public class FaroResetPasswordData {
    private String oldPassword;
    private String newPassword;

    public FaroResetPasswordData(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    private FaroResetPasswordData() {}

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}
