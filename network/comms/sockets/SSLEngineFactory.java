package network.comms.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.Security;
import java.security.Provider;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import java.lang.SecurityException;
import java.lang.NullPointerException;
import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStoreException;
import javax.net.ssl.TrustManagerFactory;
import java.lang.IllegalArgumentException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;


class SSLEngineFactory {
  public static SSLEngine newEngine(String ip, int port) {
    char []  passphrase = "123456".toCharArray();
    KeyStore ks_keys    = SSLEngineFactory.loadKeys("server.keys", passphrase);
    KeyStore ks_trust   = SSLEngineFactory.loadKeys("truststore", passphrase);
    if (ks_keys == null || ks_trust == null) {
      return null;
    }

    KeyManagerFactory   kmf = SSLEngineFactory.loadKeyManager("PKIX", ks_keys, passphrase);
    TrustManagerFactory tmf = SSLEngineFactory.loadTrustManager("PKIX", ks_trust);
    if (kmf == null || tmf == null) {
      return null;
    }

    SSLContext context = SSLEngineFactory.loadContext("TLSv1.2", kmf.getKeyManagers(), tmf.getTrustManagers());
    if (context == null) {
      return null;
    }

    return SSLEngineFactory.createEngine(context, ip, port);
  }

  private static SSLEngine createEngine(SSLContext context, String ip, int port) {
    String err_msg;

    try {
      SSLEngine engine = context.createSSLEngine(ip, port);
      return engine;
    }
    catch (UnsupportedOperationException err) {
      err_msg = "Underlying provider does not support operation!\n - " + err.getMessage();
    }
    catch (IllegalStateException err) {
      err_msg = "init() has not been called!\n - " + err.getMessage();
    }
    catch (IllegalArgumentException err) {
      err_msg = "Mode change after initial handshake!\n - " + err.getMessage();
    }

    System.err.println(err_msg);
    return null;
  }

  private static SSLContext loadContext(String algorithm, KeyManager[] key_managers, TrustManager[] trust_managers) {
    String err_msg;

    try {
      SSLContext context = SSLContext.getInstance(algorithm);
      context.init(key_managers, trust_managers, null);
      return context;
    }
    catch (KeyManagementException err) {
      err_msg = "Management error!\n - " + err.getMessage();
    }
    catch (NoSuchAlgorithmException err) {
      err_msg = "Specified algorithm not supported!\n - " + err.getMessage();
    }
    catch (NullPointerException err) {
      err_msg = "Algorithm is null!\n - " + err.getMessage();
    }

    System.err.println(err_msg);
    return null;
  }

  private static KeyStore loadKeys(String file_name, char[] passphrase) {
    String err_msg;

    try {
      KeyStore ks = KeyStore.getInstance("JKS");
      ks.load(new FileInputStream(file_name), passphrase);
      return ks;
    }
    catch (KeyStoreException err) {
      err_msg = "No Provider supports a KeyStoreSpi implementation!\n - " + err.getMessage();
    }
    catch (IllegalArgumentException err) {
      err_msg = "File does not exist!\n - " + err.getMessage();
    }
    catch (NullPointerException err) {
      err_msg = "File is null!\n - " + err.getMessage();
    }
    catch (SecurityException err) {
      err_msg = "Security manager denied access!\n - " + err.getMessage();
    }
    catch (CertificateException err) {
      err_msg = "The certificate could not be loaded!\n - " + err.getMessage();
    }
    catch (NoSuchAlgorithmException err) {
      err_msg = "Algorithm not found!\n - " + err.getMessage();
    }
    catch (IOException err) {
      err_msg = "I/O error while!\n - " + err.getMessage();
    }

    System.err.println(err_msg);
    return null;
  }

  private static KeyManagerFactory loadKeyManager(String algorithm, KeyStore key, char[] passphrase) {
    String err_msg;

    try {
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
      kmf.init(key, passphrase);
      return kmf;
    }
    catch (NoSuchAlgorithmException err) {
      err_msg = "No such algorithm!\n - " + err.getMessage();
      for (Provider p : Security.getProviders()) {
        System.out.println("Providers = " + p);
      }
    }
    catch (UnrecoverableKeyException err) {
      err_msg = "Key could not be recovered! (Password may be wrong)\n - " + err.getMessage();
    }
    catch (KeyStoreException err) {
      err_msg = "Could not initialize key stores!\n - " + err.getMessage();
    }

    System.err.println(err_msg);
    return null;
  }

  private static TrustManagerFactory loadTrustManager(String algorithm, KeyStore key) {
    String err_msg;

    try {
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
      tmf.init(key);
      return tmf;
    }
    catch (NoSuchAlgorithmException err) {
      err_msg = "No such algorithm!\n - " + err.getMessage();
      for (Provider p : Security.getProviders()) {
        System.out.println("Providers = " + p);
      }
    }
    catch (KeyStoreException err) {
      err_msg = "Could not initialize key stores!\n - " + err.getMessage();
    }

    System.err.println(err_msg);
    return null;
  }
}
