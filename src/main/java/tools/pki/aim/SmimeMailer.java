package tools.pki.aim;

import org.spongycastle.mail.smime.SMIMEException;

import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Enumeration;

import javax.mail.MessagingException;

import tools.pki.aim.Smime.SecureMailMessage;

/**
 * Created by root on 05/07/2015.
 */
public class SmimeMailer extends MimeMailer {
    private  KeyPair signerKeyPair;
    private KeyStore signerKeyStore;
    private X509Certificate signerCertificate;
    private X509Certificate recieverCertificate;
    private boolean sign;
    private boolean encrypt;

    public SmimeMailer( String host, int port, String userName, String passWord, SslMode sslType , KeyStore signerKey , String keyPin, X509Certificate recipientKey) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        super(host, port, userName, passWord, sslType);

       SetRecieverCertificate(recipientKey);

        SetKeyStore(signerKey, keyPin);

    }

    public SmimeMailer( String host, int port, String userName, String passWord, SslMode sslType) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        this(host,port,userName,passWord,sslType,null,null,null);
    }

    public void SetRecieverCertificate(X509Certificate reciverCert){
        if (reciverCert!=null){
            recieverCertificate = reciverCert;
            encrypt = true;
        }
    }



    public void SetKeyStore(KeyStore signerKey, String keyPin) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        boolean found = false;
if (signerKey!=null){
        signerKeyStore = signerKey;
        Enumeration<String> aliases = signerKey.aliases();
        while(aliases.hasMoreElements() && !found) {
            String alias = aliases.nextElement();
            System.out.println(alias);
            Key key = signerKeyStore.getKey(alias, keyPin.toCharArray());

            PrivateKey privKey = (PrivateKey) key;
            // Build CMS
            X509Certificate cert = (X509Certificate) signerKey
                    .getCertificate(alias);

            if(key!=null&& cert!=null){
                RSAPublicKey pubKey = (RSAPublicKey) cert
                        .getPublicKey();
                signerKeyPair = new KeyPair(pubKey, privKey);
                signerCertificate= cert;
                found = true;
            }
        }}

        sign = found;
    }

    public AbstractMailMessage GenerateMessage(String from, String to, String subject, String body) throws MessagingException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, CertificateEncodingException, SMIMEException, NoSuchProviderException, IOException {
        SecureMailMessage secureMailMessage = new SecureMailMessage(createMailProps(),networkCredentials,from,to, subject,  body,  signerKeyPair , signerCertificate, recieverCertificate);
    if (isSigning() ){
         secureMailMessage.Sign();
    }
     if (isSigning()&& isEncrypting()){
        secureMailMessage.Encrypt();
    }
        return secureMailMessage;
    }

    private boolean isEncrypting() {
        return encrypt;
    }

    private boolean isSigning() {
        return sign;
    }
}
