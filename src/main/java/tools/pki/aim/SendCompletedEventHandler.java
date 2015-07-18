package tools.pki.aim;

import javax.mail.MessagingException;

/**
 * Created by root on 05/07/2015.
 */
public interface SendCompletedEventHandler {
    void OnSucssessfulSent();

    void OnFail(MessagingException e);
}
