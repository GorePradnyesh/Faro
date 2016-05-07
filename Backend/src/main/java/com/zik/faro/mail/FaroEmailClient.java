package com.zik.faro.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Created by granganathan on 4/1/15.
 */

public class FaroEmailClient {
    private String messageBody;

    public void sendEmail() {
        String msgBody = "Welcomeee";
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("admin@example.com", "faro.com Admin"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("gaurav.ranganathan@gmail.com", "Mr. Gr"));
            msg.setSubject("Your faro.com account has been activated");
            msg.setText(msgBody);
            Transport.send(msg);
        } catch (AddressException e) {
            // ...
            e.printStackTrace();
        } catch (MessagingException e) {
            // ...
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
