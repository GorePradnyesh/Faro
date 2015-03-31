package com.zik.faro.api.unit;

import com.zik.faro.auth.PasswordManager;
import org.junit.Assert;
import org.junit.Test;

import java.text.MessageFormat;

/**
 * Created by granganathan on 2/28/15.
 */
public class PasswordManagerTest {

    @Test
    public void encryptPasswordTest() throws Exception {
        String password = "testPassword234!#";
        String encryptedPassword = PasswordManager.getEncryptedPassword(password);

        // Verify the returned encrypted password is not null
        Assert.assertNotNull(encryptedPassword);

        // Verify the password matches encrypted password
        Assert.assertTrue(PasswordManager.checkPasswordEquality(password, encryptedPassword));

        String anotherPassword = "anotherTestPassword628#$%%";
        String anotherEncryptedPassword = PasswordManager.getEncryptedPassword(anotherPassword);

        // Verify the encryption gets different results for different passwords
        Assert.assertNotEquals(anotherEncryptedPassword, encryptedPassword);

        System.out.println(MessageFormat.format("Password : {0} encryptedPassword : {1}",
                password, encryptedPassword));
    }

    @Test
    public void repeatedEncryptionTest() throws Exception {
        String password = "testPassword234!#";
        String encryptedPassword = PasswordManager.getEncryptedPassword(password);

        // Verify the one way digest generated is always the same for the same password
        int repeatCount = 0;
        for (int i = 0; i < repeatCount; i++) {
            Assert.assertEquals(encryptedPassword, PasswordManager.getEncryptedPassword(password));
        }
    }

}
