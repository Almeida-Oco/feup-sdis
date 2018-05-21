package network.comms;

import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import javax.net.ssl.SSLSocket;
import java.io.InputStreamReader;
import java.lang.SecurityException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;

public class Server {
  SSLServerSocket ssl_socket;

  public static void main(String[] args) {
    System.setProperty("javax.net.ssl.trustStore", "./network/comms/truststore");
    System.setProperty("javax.net.ssl.trustStorePassword", "123456");
    System.setProperty("javax.net.ssl.keyStore", "./network/comms/server.keys");
    System.setProperty("javax.net.ssl.keyStorePassword", "123456");

    Server server = Server.newServer(8080);

    if (server == null) {
      return;
    }
    System.out.println((String)server.read());
  }

  private Server() {
    this.ssl_socket = null;
  }

  public static Server newServer(int port) {
    Server temp    = new Server();
    String err_msg = null;

    try {
      temp.ssl_socket = (SSLServerSocket)SSLServerSocketFactory.getDefault().createServerSocket(port);
      return temp;
    }
    catch (IllegalArgumentException err) {
      err_msg = "Port number must be between [0, 65535], got " + port;
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

  public String read() {
    SSLSocket      socket;
    BufferedReader in;

    if ((socket = this.accept()) == null) {
      return null;
    }

    try {
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      String line = in.readLine();
      return line;
    }
    catch (IOException err) {
      System.err.println("Socket closed while getting input stream!\n - " + err.getMessage());
      return null;
    }
  }

  public void listen() {
    SSLSocket socket;

    while (true) {
      if ((socket = this.accept()) == null) {
        return;
      }
    }
  }

  private SSLSocket accept() {
    String err_msg = null;

    try {
      return (SSLSocket)this.ssl_socket.accept();
    }
    catch (IllegalBlockingModeException err) {
      err_msg = "No connection ready to be accepted!\n - " + err.getMessage();
    }
    catch (SocketTimeoutException err) {
      err_msg = "Socket accept timed-out!\n - " + err.getMessage();
    }
    catch (SecurityException err) {
      err_msg = "Operation not allowed by security manager!\n - " + err.getMessage();
    }
    catch (IOException err) {
      err_msg = "I/O error occurred while waiting for connection!\n - " + err.getMessage();
    }
    System.err.println(err_msg);
    return null;
  }
}
