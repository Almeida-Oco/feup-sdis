package network.comms.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.lang.SecurityException;
import javax.net.ssl.SSLEngineResult;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLEngineResult.Status;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.UnsupportedAddressTypeException;

public class SSLSocketChannel extends SSLChannel {
  private static final int BUF_SIZE = 32768;
  SSLEngine engine;
  SocketChannel socket;

  private SSLSocketChannel(SocketChannel socket, SSLEngine engine) {
    super(socket, engine, null);
  }

  public static SSLSocketChannel newChannel(String addr, int port, boolean client_mode) {
    SocketChannel socket;

    if ((socket = SSLChannel.openChannel(addr, port)) == null) {
      return null;
    }

    return SSLSocketChannel.newChannel(socket, addr, port, client_mode);
  }

  public static SSLSocketChannel newChannel(SocketChannel socket, String addr, int port, boolean client_mode) {
    SSLEngine engine;

    if ((engine = SSLEngineFactory.newEngine(addr, port)) == null) {
      return null;
    }

    engine.setUseClientMode(client_mode);
    SSLSocketChannel socket_channel = new SSLSocketChannel(socket, engine);
    try {
      if (!socket_channel.doHandshake()) {
        System.out.println("Failed to handshake!");
        return null;
      }
      else {
        System.out.println("Handshake is done boie!");
        return socket_channel;
      }
    }
    catch (IOException err) {
      System.err.println("IO Error while handshaking!\n - " + err.getMessage());
      return null;
    }
  }

  public String getID() {
    try {
      InetSocketAddress addr = (InetSocketAddress)this.socket.getLocalAddress();

      return addr.getHostName() + ":" + addr.getPort();
    }
    catch (IOException err) {
      System.err.println("Failed to get SSLSocketChannel ID!\n - " + err.getMessage());
    }
    return null;
  }

  public boolean sendMsg(String msg) {
    String err_msg;

    try {
      this.my_app_data.clear();
      this.my_app_data.put(msg.getBytes());
      this.my_app_data.flip();
      return this.sendData();
    }
    catch (SSLException err) {
      err_msg = "Problem occurred while beginning handshake!\n  - " + err.getMessage();
    }
    catch (IllegalStateException err) {
      err_msg = "Client/Server mode not set!\n - " + err.getMessage();
    }
    catch (IOException err) {
      err_msg = "I/O error while sending message!\n - " + err.getMessage();
    }

    System.err.println(err_msg);
    return false;
  }

  private boolean sendData() throws IOException, SSLException {
    while (this.my_app_data.hasRemaining()) {
      this.my_net_data.clear();
      SSLEngineResult res = this.engine.wrap(this.my_app_data, this.my_net_data);
      System.out.println(res);

      if (res.getStatus() == Status.OK) {
        this.my_net_data.flip();

        System.out.println("Sending my_net_data");
        while (this.my_net_data.hasRemaining()) {
          this.socket.write(this.my_net_data);
        }
        System.out.println("Sent my_net_data");
        return true;
      }
      else {
        System.out.println("While sending got '" + res.getStatus() + "'");
      }
    }

    return false;
  }

  public String recvMsg() {
    int num;

    this.my_net_data.clear();
    this.my_app_data.clear();
    try {
      num = this.socket.read(this.my_net_data);
    }
    catch (IOException err) {
      System.err.println("I/O error while receiving message!\n - " + err.getMessage());
      return null;
    }
    catch (NotYetConnectedException err) {
      // System.err.println("Not yet connected!\n - " + err.getMessage());
      return null;
    }

    if (num == -1) {
      System.err.println("Socket closed while receiving message!");
      return null;
    }
    else if (num == 0) {
      System.err.println("Nothing to read");
      return null;
    }
    else {
      return this.decodeData();
    }
  }

  private String decodeData() {
    String result = null;

    this.my_net_data.flip();
    SSLEngineResult res;
    try {
      while (this.my_net_data.hasRemaining()) {
        this.my_app_data.clear();
        res = this.engine.unwrap(this.my_net_data, this.my_app_data);

        if (res.getStatus() == SSLEngineResult.Status.OK) {
          this.my_app_data.flip();
          if (result == null) {
            result = new String(this.my_app_data.array());
          }
          else {
            result += new String(this.my_app_data.array());
          }
        }
        else {
          System.out.println("Read status = " + res.getStatus());
        }
      }
      return result;
    }
    catch (SSLException err) {
      System.err.println("SSL error while unwrapping!\n - " + err.getMessage());
    }

    return null;
  }
}
