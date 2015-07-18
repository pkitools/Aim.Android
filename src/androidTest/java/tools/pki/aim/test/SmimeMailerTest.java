package tools.pki.aim.test;

import junit.framework.TestCase;

import tools.pki.aim.MimeMailer;
import tools.pki.aim.SmimeMailer;

/**
 * Created by root on 06/07/2015.
 */
public class SmimeMailerTest extends TestCase {


    public void testSetRecieverCertificate() throws Exception {

        SmimeMailer mailer = new SmimeMailer("smtp.pki.tools",587,"test@pki.tools","testpass", MimeMailer.SslMode.None);
    }

    public void testSetKeyStore() throws Exception {

    }

    public void testGenerateMessage() throws Exception {

    }
}