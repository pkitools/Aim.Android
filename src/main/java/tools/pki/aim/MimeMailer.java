package tools.pki.aim;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;

/**
 * Created by root on 05/07/2015.
 */
public class MimeMailer {
    private static final Object DEBUG_SMTP = true;
    protected  String host;
    protected int port;
    protected String user;
    protected String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public SslMode getSslType() {
        return sslType;
    }

    public void setSslType(SslMode sslType) {
        this.sslType = sslType;
    }

    public javax.mail.Authenticator getNetworkCredentials() {
        return networkCredentials;
    }

    public void setNetworkCredentials(javax.mail.Authenticator networkCredentials) {
        this.networkCredentials = networkCredentials;
    }

    public enum SslMode
    {
        /// <summary>
/// None Ssl Servers
/// </summary>
        None ,
/// <summary>
/// Explicit Ssl Servers
/// </summary>
        Ssl  ,
/// <summary>
/// Implicit Ssl Servers
/// </summary>
        Tls
/// <summary>
/// todo:Authomaticaly detect type of ssl
/// </summary>
    }
    protected SslMode sslType = SslMode.None;
    protected javax.mail.Authenticator networkCredentials;
    public MimeMailer(String host, int port , final String userName , final String passWord , SslMode sslType ) {
        this.setHost(host);
        this.setPort(port);
        this.user = userName;
        this.password = passWord;
        this.setSslType(sslType);
        if (user != null && passWord != null) {
            setNetworkCredentials(new javax.mail.Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            userName,
                            passWord);
                }
            });

        }
    }

    protected Properties createMailProps() {
        Properties props = new Properties();


        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", getHost());
        props.put("mail.smtp.port", getPort());
        props.put("mail.smtp.debug", DEBUG_SMTP);


        props.put("mail.debug", DEBUG_SMTP);

        if (getSslType() == SslMode.Ssl || getSslType() == SslMode.Tls) {
            props.put("mail.smtp.socketFactory.port", String.valueOf(getPort()));
            props.put("mail.smtp.socketFactory.class",       "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");

            if (getSslType() == SslMode.Tls){
                props.put("mail.smtp.starttls.enable","true");
            }
        }
        //     props.setProperty("mail.smtp.quitwait","false");
        return props;
    }

    /// <summary>
/// Send message to the server
/// </summary>
/// <param name="message">Email message that we want to send</param>
/// <param name="onSendCallBack">The deligated function which will be called after sending message.</param>
    public void Send(AbstractMailMessage message, SendCompletedEventHandler onSendCallBack)  {

        try {
            Transport.send(message);
            onSendCallBack.OnSucssessfulSent();
        } catch (MessagingException e) {
            onSendCallBack.OnFail(e);
        }


    }
}
