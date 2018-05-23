package network.comms;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.io.BufferedReader;
import javax.net.ssl.SSLSocket;
import java.io.InputStreamReader;
import java.lang.SecurityException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;


public class Connection {
  OutputStream out;
  BufferedReader in;

  SSLSocket socket;
  InetAddress addr;
  int port;


  private Connection(InetAddress addr, int port, SSLSocket socket) {
    this.socket = socket;
    this.port   = port;
    this.addr   = addr;
  }

  public static Connection connect(String ip, int port) {
    String err_msg = null;

    try {
      InetAddress addr   = InetAddress.getByAddress(ip.getBytes());
      SSLSocket   socket = (SSLSocket)SSLSocketFactory.getDefault().createSocket(addr, port);
      Connection  conn   = new Connection(addr, port, socket);

      conn.out = socket.getOutputStream();
      conn.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
      err_msg = "Error while creating SSLSocket!\n - " + err.getMessage();
    }

    System.err.println(err_msg);
    return null;
  }

  public String getIP() {
    return this.addr.toString();
  }

  public boolean sendMsg(String msg) {
    try {
      this.out.write(msg.getBytes());
      return true;
    }
    catch (IOException err) {
      System.err.println("Socket closed while sending message!\n - " + err.getMessage());
    }
    return false;
  }

  public String getLine() {
    try {
      return this.in.readLine();
    }
    catch (IOException err) {
      System.err.println("Failed to read line!\n - " + err.getMessage());
    }
    return null;
  }

  public boolean closeConn() {
    try {
      this.socket.close();
      return true;
    }
    catch (IOException err) {
      System.err.println("Failed to close connection!\n - " + err.getMessage());
    }
    return false;
  }
}
