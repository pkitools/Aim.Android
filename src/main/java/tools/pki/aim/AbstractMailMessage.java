package tools.pki.aim;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by root on 05/07/2015.
 */
public  abstract class AbstractMailMessage extends MimeMessage{

    public AbstractMailMessage(Properties properties, Authenticator auth, String from,String to,String subject,String body) throws MessagingException {
        super(Session.getDefaultInstance(properties,auth));
        Address fromUser = new InternetAddress(from);
        Address toUser = new InternetAddress();
        setFrom(fromUser);
       setRecipient(Message.RecipientType.TO, toUser);
        setSubject(subject);
        setContent(body, "text/plain");
        saveChanges();

    }
    public  Session getSession(){
        return  session;
    }
}
