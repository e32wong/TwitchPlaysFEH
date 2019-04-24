package com.mycompany.app;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class SocketInitializer implements Runnable {

    SocketServerSystem socketSystem = null;
    String baseConfigDir;
    int websocketPort;
    EventSystem eventSystem;
    boolean debug;
    String twitchClientID;
    String twitchExtensionSecret;

    public SocketInitializer (int websocketPort, String baseConfigDir, boolean debug,
            String twitchClientID, String twitchExtensionSecret) {
        this.baseConfigDir = baseConfigDir;
        this.websocketPort = websocketPort;
        this.debug = debug;
        this.twitchClientID = twitchClientID;
        this.twitchExtensionSecret = twitchExtensionSecret;
    }

    public void addEventSystem(EventSystem eventSystem) {
        this.eventSystem = eventSystem;

    }

    public void run() {

        System.out.println("Socket initializer thread running..");

        startSystem();

        // keep testing if the socket system is down
        while (true) {

            boolean isConnected = socketSystem.isConnected();
            if (!isConnected) {

                System.out.println("socket system error detected, recovering");

                try {
                    // kill the old one
                    socketSystem.stop(1000);
                    Thread.sleep(1000);

                    // create new one
                    socketSystem = startSystem();

                } catch (Exception e) {

                    System.out.println("Error at reconnecting");
                }
            }

			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println("Error at sleep");
			}
        }

    }

    public SocketServerSystem getSocketSystem() {

        try {
            while (socketSystem == null || socketSystem.isConnected() == false) {
                System.out.println("Socket system still down..");
                Thread.sleep(1000);
            }

            return socketSystem;

        } catch (Exception e) {
            System.out.println("Error at retrieving socket system");
            e.printStackTrace();
        }

        return null;
    }

    public SocketServerSystem startSystem() {

        try {

            System.out.println("Starting the socket server system");

            socketSystem = new SocketServerSystem(websocketPort, baseConfigDir, eventSystem,
                    debug, twitchClientID, twitchExtensionSecret);

            SSLContext context = getContext(baseConfigDir + "/pem");
            if( context != null ) {
                socketSystem.setWebSocketFactory( new DefaultSSLWebSocketServerFactory( getContext(baseConfigDir + "/pem") ) );
            }
            socketSystem.setConnectionLostTimeout(15);
            socketSystem.setReuseAddr(true);
            socketSystem.start();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        System.out.println("Closing the server..");
                        socketSystem.stop(5000);
                        System.out.println("Closed.");

                    } catch (Exception e) {
                        System.out.println("IO exception while closing");
                    }
                }});

			Thread.sleep(2000);

            boolean isConnected = socketSystem.isConnected();
            if (!isConnected) {
                return null;
            } else {
                return socketSystem;
            }

        } catch (Exception e) {
            System.out.println("Error at starting server");
        }

		return null;
    }

    private static SSLContext getContext(String pemPath) {
        SSLContext context;
        String password = "CHANGEIT";
        try {
            context = SSLContext.getInstance("TLS");

            byte[] certBytes = parseDERFromPEM( getBytes(
                        new File( pemPath + File.separator + "fullchain.pem" ) ),
                    "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----" );
            byte[] keyBytes = parseDERFromPEM( getBytes(
                        new File( pemPath + File.separator + "privkey.pem" ) ),
                    "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----" );


            X509Certificate cert = generateCertificateFromDER( certBytes );
            RSAPrivateKey key = generatePrivateKeyFromDER( keyBytes );

            KeyStore keystore = KeyStore.getInstance( "JKS" );
            keystore.load( null );
            keystore.setCertificateEntry( "cert-alias", cert );
            keystore.setKeyEntry( "key-alias", key, password.toCharArray(), new Certificate[]{ cert } );

            KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
            kmf.init( keystore, password.toCharArray() );

            KeyManager[] km = kmf.getKeyManagers();

            context.init( km, null, null );
        } catch ( Exception e ) {
            context = null;
        }
        return context;
	}

    private static byte[] parseDERFromPEM( byte[] pem, String beginDelimiter, String endDelimiter ) {
        String data = new String( pem );
        String[] tokens = data.split( beginDelimiter );
        tokens = tokens[1].split( endDelimiter );
        return DatatypeConverter.parseBase64Binary( tokens[0] );
    }

    private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory factory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey)factory.generatePrivate(spec);
    }

    private static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {

        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
    }

    private static byte[] getBytes(final File file) {
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(bytesArray);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytesArray;
    }

}
