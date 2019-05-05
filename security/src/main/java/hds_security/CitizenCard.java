package hds_security;

import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sun.security.pkcs11.wrapper.*;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CitizenCard{

    public byte[] signMessage(byte[] message) throws PteidException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, PKCS11Exception {

        System.loadLibrary("pteidlibj");
        pteid.Init("");
        pteid.SetSODChecking(false);
        PKCS11 pkcs11;
        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");

        String libName = "libpteidpkcs11.so";
        if (osName.contains("Windows"))
            libName = "pteidpkcs11.dll";
        else if (osName.contains("Mac"))
            libName = "pteidpkcs11.dylib";

        Class<? extends Object> pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
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

        pkcs11.C_FindObjectsInit(p11_session, attributes);
        long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);
        long signatureKey = keyHandles[0];
        pkcs11.C_FindObjectsFinal(p11_session);

        CK_MECHANISM mechanism = new CK_MECHANISM();
        mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
        mechanism.pParameter = null;
        pkcs11.C_SignInit(p11_session, mechanism, signatureKey);

        byte[] signature = pkcs11.C_Sign(p11_session, message);

        byte[] encodedCert = getCitizenAuthCertInBytes();
        try {
            X509Certificate certificate = getCertFromByteArray(encodedCert);
            PublicKey serverPublicKey = certificate.getPublicKey();
            storePublicKey(serverPublicKey);
        } catch (CertificateException | IOException e) {
            e.printStackTrace();
        }

        pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //OBRIGATORIO Termina a eID Lib
        return signature;

    }

    /* verifies if the server public key already exists, if not it writes into it
    * bare in mind the public key is managed differently than the regular PublicKey in java.crypto,
    * in PKCS11 is just represented as a long */

    private void storePublicKey(PublicKey publicKey) throws IOException {
    	// Store in User
    	try (FileOutputStream fos = new FileOutputStream("../user/src/main/resources/serverPublicKey.txt");) {
			fos.write(publicKey.getEncoded());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
    	// Store in Notary
    	try (FileOutputStream fos = new FileOutputStream("./src/main/resources/serverPublicKey.txt");) {
			fos.write(publicKey.getEncoded());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
    }

    //Returns the CITIZEN AUTHENTICATION CERTIFICATE
    private static byte[] getCitizenAuthCertInBytes(){
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

        } catch (PteidException e) {
            e.printStackTrace();
        }
        return certificate_bytes;
    }


    private static X509Certificate getCertFromByteArray(byte[] certificateEncoded) throws CertificateException{
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(certificateEncoded);
        X509Certificate cert = (X509Certificate)f.generateCertificate(in);
        return cert;
    }
}
