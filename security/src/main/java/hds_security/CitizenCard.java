package hds_security;

import pteidlib.PTEID_ADDR;
import pteidlib.PTEID_Certif;
import pteidlib.PTEID_ID;
import pteidlib.PTEID_PIC;
import pteidlib.PTEID_Pin;
import pteidlib.PTEID_TokenInfo;
import pteidlib.PteidException;
import pteidlib.pteid;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.lang.reflect.Method;
import javax.crypto.*;

import sun.security.pkcs11.wrapper.*;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


@SuppressWarnings("Duplicates")
public class CitizenCard{

    public byte[] signMessage(byte[] message) throws PteidException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, PKCS11Exception {


        System.loadLibrary("pteidlibj");
        pteid.Init("");
        pteid.SetSODChecking(false);


        PKCS11 pkcs11;
        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

        String libName = "libbeidpkcs11.so";
        if (osName.contains("Windows"))
            libName = "pteidpkcs11.dll";
        else if (osName.contains("Mac"))
            libName = "pteidpkcs11.dylib";

        Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
        if (javaVersion.startsWith("1.5."))
        {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
            pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, null, false });
        }
        else
        {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
            pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });
        }


        long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);


        pkcs11.C_Login(p11_session, 1, null);
        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
        attributes[0] = new CK_ATTRIBUTE();
        attributes[0].type = PKCS11Constants.CKA_CLASS;
        attributes[0].pValue = PKCS11Constants.CKO_PRIVATE_KEY;
        long publicKey = PKCS11Constants.CKO_PUBLIC_KEY;

        try {
            storePublicKey(publicKey);
        } catch (IOException e) {
            e.printStackTrace();
        }

        pkcs11.C_FindObjectsInit(p11_session, attributes);
        long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);
        long signatureKey = keyHandles[0];
        pkcs11.C_FindObjectsFinal(p11_session);

        CK_MECHANISM mechanism = new CK_MECHANISM();
        mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
        mechanism.pParameter = null;
        pkcs11.C_SignInit(p11_session, mechanism, signatureKey);

        byte[] signature = pkcs11.C_Sign(p11_session, message);

        pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //OBRIGATORIO Termina a eID Lib
        return signature;

    }


    public boolean verifyMessage(long publicKey, byte[] original, byte[] signedMessage) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PKCS11 pkcs11;
        String osName = System.getProperty("os.name");
        String libName = "libbeidpkcs11.so";
        String javaVersion = System.getProperty("java.version");

        if (osName.contains("Windows"))
            libName = "pteidpkcs11.dll";
        else if (osName.contains("Mac"))
            libName = "pteidpkcs11.dylib";
        Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
        if (javaVersion.startsWith("1.5."))
        {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
            pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, null, false });
        }
        else
        {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
            pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });
        }
        CK_MECHANISM mechanism = new CK_MECHANISM();
        mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
        mechanism.pParameter = null;
        pkcs11.C_VerifyInit();
       return false;
    }


    /* verifies if the server public key already exists, if not it writes into it
    * bare in mind the public key is managed differently than the regular PublicKey in java.crypto,
    * in PKCS11 is just represented as a long */

    private void storePublicKey(long publicKey) throws IOException {
        File f = new File("../resources/serverPublicKey.txt");
        if(f.exists()){
            return;
        }
        else{
            f.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(f);
        DataOutputStream dos = new DataOutputStream(fos);
        dos.writeLong(publicKey);
        dos.close();
        fos.close();
    }


    //Returns the CITIZEN AUTHENTICATION CERTIFICATE
    public static byte[] getCitizenAuthCertInBytes(){
        return getCertificateInBytes(0); //certificado 0 no Cartao do Cidadao eh o de autenticacao
    }

    // Returns the n-th certificate, starting from 0
    private static  byte[] getCertificateInBytes(int n) {
        byte[] certificate_bytes = null;
        try {
            PTEID_Certif[] certs = pteid.GetCertificates();
            System.out.println("Number of certs found: " + certs.length);
            int i = 0;
            for (PTEID_Certif cert : certs) {
                System.out.println("-------------------------------\nCertificate #"+(i++));
                System.out.println(cert.certifLabel);
            }

            certificate_bytes = certs[n].certif; //gets the byte[] with the n-th certif

            //pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); // OBRIGATORIO Termina a eID Lib
        } catch (PteidException e) {
            e.printStackTrace();
        }
        return certificate_bytes;
    }

    public static X509Certificate getCertFromByteArray(byte[] certificateEncoded) throws CertificateException{
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(certificateEncoded);
        X509Certificate cert = (X509Certificate)f.generateCertificate(in);
        return cert;
    }
}
