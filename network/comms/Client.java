package network.comms;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.net.ssl.SSLSocket;
import java.lang.SecurityException;
import javax.net.ssl.SSLSocketFactory;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;

public class Client {
  SSLSocket ssl_socket;

  public static void main(String[] args) {
    System.setProperty("javax.net.ssl.trustStore", "./network/comms/truststore");
    System.setProperty("javax.net.ssl.trustStorePassword", "123456");
    System.setProperty("javax.net.ssl.keyStore", "./network/comms/server.keys");
    System.setProperty("javax.net.ssl.keyStorePassword", "123456");

    Client client = Client.newClient("127.0.0.1", 8080);
    if (client == null) {
      return;
    }
    else {
      System.out.println("Created client");
    }

    client.write("Hola como estas?");
  }

  private Client() {
    this.ssl_socket = null;
  }

  public static Client newClient(String addr, int port) {
    Client temp    = new Client();
    String err_msg = null;

    try {
      temp.ssl_socket = (SSLSocket)SSLSocketFactory.getDefault().createSocket(addr, port);
      return temp;
    }
    catch (IllegalArgumentException err) {
      err_msg = "Port number must be between [0, 65535], got " + port;
    }
    catch (UnknownHostException err) {
      err_msg = "Unknown host!\n - " + err.getMessage();
    }
    catch (SecurityException err) {
      err_msg = "Operation not allowed by security manager!\n - " + err.getMessage();
    }
    catch (IOException err) {
      err_msg = "Error while creating SSLServerSocket!\n - " + err.getMessage();
    }


    System.err.println(err_msg);
    return null;
  }

  public void write(Object content) {
    try {
      OutputStream out = this.ssl_socket.getOutputStream();
      System.out.println("IM HERE");
      out.write("Hola como estas?\n".getBytes());
    }
    catch (IOException err) {
      System.err.println("Socket closed while getting output stream!\n - " + err.getMessage());
    }
  }
}
