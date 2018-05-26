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

  private SSLSocketChannel(SocketChannel socket, SSLEngine engine) {
    super(socket, engine);
  }

  public static SSLSocketChannel newChannel(String addr, int port, boolean client_mode) {
    SocketChannel socket;

    if ((socket = SSLChannel.openChannel(addr, port)) == null) {
      return null;
    }

    return SSLSocketChannel.newChannel(socket, client_mode);
  }

  public static SSLSocketChannel newChannel(SocketChannel socket, boolean client_mode) {
    SSLEngine         engine;
    InetSocketAddress addr;

    try {
      socket.configureBlocking(false);
      addr = (InetSocketAddress)socket.getLocalAddress();
    }
    catch (IOException err) {
      System.err.println("Error getting local address!\n - " + err.getMessage());
      return null;
    }

    if ((engine = SSLEngineFactory.newEngine(addr.getHostString(), addr.getPort())) == null) {
      return null;
    }

    engine.setUseClientMode(client_mode);
    SSLSocketChannel socket_channel = new SSLSocketChannel(socket, engine);

    try {
      if (socket_channel.doHandshake() && socket_channel.setupID()) {
        return socket_channel;
      }
      System.err.println("Failed to handshake!");
    }
    catch (IOException err) {
      System.err.println("IO Error while handshaking!\n - " + err.getMessage());
    }
    return null;
  }

  private boolean setupID() {
    try {
      InetSocketAddress addr = (InetSocketAddress)this.socket.getLocalAddress();

      this.channel_id = addr.getHostName() + ":" + addr.getPort();
      return true;
    }
    catch (IOException err) {
      System.err.println("Failed to get SSLSocketChannel ID!\n - " + err.getMessage());
    }
    return false;
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

      if (res.getStatus() == Status.OK) {
        this.my_net_data.flip();
        while (this.my_net_data.hasRemaining()) {
          this.socket.write(this.my_net_data);
        }
        return true;
      }
      else {
        this.handleSendNonOkStatus(res.getStatus());
      }
    }

    return false;
  }

  public String recvMsg() {
    try {
      int bytes_read;
      this.peer_net_data.clear();
      if ((bytes_read = this.socket.read(this.peer_net_data)) > 0) {
        return this.decodeData();
      }
      else if (bytes_read < 0) {
        System.err.println("Socket closed while receiving message!");
        return null;
      }
    }
    catch (IOException err) {
      System.err.println("I/O error while receiving message!\n - " + err.getMessage());
    }
    catch (NotYetConnectedException err) {
      System.err.println("Not yet connected!\n - " + err.getMessage());
    }
    return null;
  }

  private String decodeData() {
    String          result = "";
    SSLEngineResult res;

    try {
      while (this.peer_net_data.hasRemaining()) {
        this.peer_app_data.clear();
        this.peer_net_data.flip();
        res = this.engine.unwrap(this.peer_net_data, this.peer_app_data);

        if (res.getStatus() == Status.OK) {
          this.peer_app_data.flip();
          String got = new String(this.peer_app_data.array());
          result += new String(got);
        }
        else {
          System.out.println("Non ok");
          this.handleRecvNonOkStatus(res.getStatus());
        }
      }
      return result;
    }
    catch (SSLException err) {
      System.err.println("SSL error while unwrapping!\n - " + err.getMessage());
    }
    catch (IOException err) {
      System.err.println("I/O error while handling recv status!\n - " + err.getMessage());
    }

    return null;
  }

  public SocketChannel getChannel() {
    return this.socket;
  }

  private void handleRecvNonOkStatus(Status status) throws IOException {
    if (status == Status.BUFFER_OVERFLOW) {
      System.out.println("Overflow");
      this.peer_app_data = ByteBuffer.allocate(this.engine.getSession().getApplicationBufferSize());
    }
    else if (status == Status.BUFFER_UNDERFLOW) {
      System.out.println("Underflow");
      this.socket.read(this.peer_net_data);
    }
    else {
      System.err.println(status);
      System.err.println("SSLSocketChannel::recv Engine closed!?");
      System.exit(5);
    }
  }

  private void handleSendNonOkStatus(Status status) {
    if (status == Status.BUFFER_OVERFLOW) {
      System.out.println("Send overflow");
      this.my_net_data = ByteBuffer.allocate(this.engine.getSession().getPacketBufferSize());
    }
    else if (status == Status.BUFFER_UNDERFLOW) {
      System.out.println("Send underflow");
    }
    else {
      System.err.println("SSLSocketChannel::send Engine closed!?");
      System.exit(5);
    }
  }
}
