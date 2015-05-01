package com.zik.faro.api.unit;

import com.zik.faro.mail.FaroEmailClient;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by granganathan on 4/2/15.
 */
@Ignore
public class FaroEmailClientTest {

    @Test
    public void sendEmailTest() {
        FaroEmailClient emailClient = new FaroEmailClient();
        emailClient.sendEmail();
    }
}
