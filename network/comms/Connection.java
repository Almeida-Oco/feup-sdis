package network.comms;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.io.BufferedReader;
import javax.net.ssl.SSLSocket;
import java.io.InputStreamReader;
import java.lang.SecurityException;
import javax.net.ssl.SSLServerSocket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLServerSocketFactory;
import java.nio.channels.IllegalBlockingModeException;


public class Connection {
  OutputStream out;
  BufferedReader in;

  SSLSocket socket;
  InetAddress remote_ip;
  int remote_port;

  private Connection(SSLSocket socket, InetAddress addr, int port) {
    this.socket      = socket;
    this.remote_ip   = addr;
    this.remote_port = port;
  }

  public static Connection connect(String ip, int port) {
    String err_msg = null;

    try {
      InetAddress addr   = InetAddress.getByAddress(ip.getBytes());
      SSLSocket   socket = (SSLSocket)SSLSocketFactory.getDefault().createSocket(addr, port);
      Connection  conn   = new Connection(socket, addr, port);

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

  public static Connection listen(int port) {
    String err_msg;

    try {
      SSLServerSocket ssocket = (SSLServerSocket)SSLServerSocketFactory.getDefault().createServerSocket(port);
      SSLSocket       socket  = (SSLSocket)ssocket.accept();
      return new Connection(socket, socket.getInetAddress(), socket.getPort());
    }
    catch (IllegalArgumentException err) {
      err_msg = "Port number must be between [0, 65535], got " + port;
    }
    catch (SocketTimeoutException err) {
      err_msg = "Socket accept timed-out!\n - " + err.getMessage();
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

  public String getIP() {
    return this.remote_ip.toString() + ":" + this.remote_port;
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
