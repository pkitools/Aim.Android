package tools.pki.aim.Smime;

import android.os.Environment;
import android.util.Log;

import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.cms.AttributeTable;
import org.spongycastle.asn1.cms.IssuerAndSerialNumber;
import org.spongycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.spongycastle.asn1.smime.SMIMECapability;
import org.spongycastle.asn1.smime.SMIMECapabilityVector;
import org.spongycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.cert.jcajce.JcaCertStore;
import org.spongycastle.cms.CMSAlgorithm;
import org.spongycastle.cms.CMSException;
import org.spongycastle.cms.SignerInfoGenerator;
import org.spongycastle.cms.bc.BcRSAKeyTransRecipientInfoGenerator;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.spongycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.mail.smime.SMIMESignedGenerator;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.OutputEncryptor;
import org.spongycastle.operator.bc.BcRSAAsymmetricKeyWrapper;
import org.spongycastle.util.Store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Certificate;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import tools.pki.aim.AbstractMailMessage;
import tools.pki.aim.Configuration;

/**
 * Created by root on 05/07/2015.
 */
public class SecureMailMessage extends AbstractMailMessage {

    public static final String SHA_512_WITH_RSA = "SHA512withRSA";

    private String algorythm = SHA_512_WITH_RSA;
    private File outPutFile;
    private  X509Certificate signerCert;
    private  KeyPair signerKeyPair;
    private X509Certificate recieverCert;
    public SecureMailMessage(Properties properties, Authenticator auth, String from, String to, String subject, String body, KeyPair keyPair ,X509Certificate signerCert , X509Certificate recieverCert) throws MessagingException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        super(properties, auth, from, to, subject, body);
                signerKeyPair = keyPair;
                this.signerCert = signerCert;
        this.recieverCert = recieverCert;

    }

    public void Sign() throws CertificateEncodingException, NoSuchAlgorithmException, SMIMEException, NoSuchProviderException, MessagingException, IOException {
        ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
        SMIMECapabilityVector caps = new SMIMECapabilityVector();

        caps.addCapability(SMIMECapability.aES256_CBC);
        caps.addCapability(SMIMECapability.dES_EDE3_CBC);
        caps.addCapability(SMIMECapability.dES_CBC);

        signedAttrs.add(new SMIMECapabilitiesAttribute(caps));

        // for encrypted responses
        IssuerAndSerialNumber issAndSer = new IssuerAndSerialNumber(
                new X500Name(signerCert.getSubjectDN().getName()),
                signerCert.getSerialNumber());
        signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(issAndSer));

        SMIMESignedGenerator gen = new SMIMESignedGenerator();
        gen.addSignerInfoGenerator(createSignerInfoGenerator(signedAttrs));

        Store certs = new JcaCertStore(Arrays.asList(signerCert));
        gen.addCertificates(certs);


        MimeMultipart mm = gen.generate(this, Configuration.PROVIDER_NAME);
       // MimeMessage signedMessage = new MimeMessage(session);
        Enumeration<?> headers = getAllHeaderLines();
       // while (headers.hasMoreElements()) {
         //   addHeaderLine((String) headers.nextElement());
       // }
        setContent(mm);
        saveChanges();

        if (outPutFile!=null) {
            writeTo(new FileOutputStream(outPutFile));
        }
    }

    protected SignerInfoGenerator createSignerInfoGenerator(
            ASN1EncodableVector signedAttrs) {
        try {
            return new JcaSimpleSignerInfoGeneratorBuilder()
                    .setProvider("AndroidOpenSSL")
                    .setSignedAttributeGenerator(
                            new AttributeTable(signedAttrs))
                    .build(algorythm, signerKeyPair.getPrivate(), signerCert);
        } catch (CertificateEncodingException e) {
            throw new RuntimeException(e);
        } catch (OperatorCreationException e) {
            throw new RuntimeException(e);
        }
    }


    protected OutputEncryptor createEnvelopedInfoGenerator(
            ASN1ObjectIdentifier encdAttr) throws CMSException {
        return new JceCMSContentEncryptorBuilder(encdAttr,40)
                .setProvider("AndroidOpenSSL")
                .build();
    }

    public void Encrypt() throws CertificateEncodingException, CMSException, SMIMEException, MessagingException, IOException {

        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
        MailcapCommandMap _mailcap=(MailcapCommandMap)CommandMap.getDefaultCommandMap();
        _mailcap.addMailcap("application/pkcs7-signature;; x-java-content-handler=org.spongycastle.mail.smime.handlers.pkcs7_signature");
        _mailcap.addMailcap("application/pkcs7-mime;; x-java-content-handler=org.spongycastle.mail.smime.handlers.pkcs7_mime");
        _mailcap.addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.spongycastle.mail.smime.handlers.x_pkcs7_signature");
        _mailcap.addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.spongycastle.mail.smime.handlers.x_pkcs7_mime");
        _mailcap.addMailcap("multipart/signed;; x-java-content-handler=org.spongycastle.mail.smime.handlers.multipart_signed");
        //originalMap=CommandMap.getDefaultCommandMap();
        CommandMap.setDefaultCommandMap(_mailcap);

        if (Security.getProvider(Configuration.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

/*        KeyStore ks = KeyStore.getInstance("PKCS12", "SC");
        ks.load(new FileInputStream("keystorefile.pfx"), "passwd".toCharArray());
        Enumeration e = ks.aliases();
        String keyAlias = null;
        while (e.hasMoreElements()) {
            String alias = (String) e.nextElement();
            if (ks.isKeyEntry(alias)) {
                keyAlias = alias;
            }
        }
        if (keyAlias == null) {
            System.err.println("can't find a private key!");
            System.exit(0);
        }*/
       // Certificate[] chain = ks.getCertificateChain(keyAlias);
        SMIMEEnvelopedGenerator gen = new SMIMEEnvelopedGenerator();
        gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator((X509Certificate) recieverCert).setProvider(Configuration.PROVIDER_NAME));
        MimeBodyPart msg = new MimeBodyPart();
        MimeBodyPart mp = gen.generate(msg, new JceCMSContentEncryptorBuilder(CMSAlgorithm.RC2_CBC).setProvider(Configuration.PROVIDER_NAME).build());
        setContent(mp.getContent(), mp.getContentType());
        saveChanges();

        if (outPutFile!=null) {
            writeTo(new FileOutputStream(outPutFile));
        }
    }
}
