package network.comms.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import javax.net.ssl.TrustManager;
import java.lang.SecurityException;
import javax.net.ssl.SSLEngineResult;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLEngineResult.Status;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.UnsupportedAddressTypeException;

public class SSLSocketChannel extends SocketChannel {
  private static final int BUF_SIZE = 32768;
  SSLEngine engine;
  SocketChannel socket;

  ByteBuffer app_buffer, net_buffer;

  private SSLSocketChannel(SocketChannel socket, SSLEngine engine) {
    this.engine     = engine;
    this.socket     = socket;
    this.app_buffer = ByteBuffer.allocate(BUF_SIZE);
    this.net_buffer = ByteBuffer.allocate(BUF_SIZE);
  }

  public static SSLSocketChannel newChannel(String addr, int port) {
    SSLEngine engine = SSLEngineFactory.newEngine(addr, port);

    engine.setUseClientMode(true);
    SocketChannel socket = SSLSocketChannel.openChannel(addr, port);

    if (socket == null || engine == null) {
      return null;
    }
    ByteBuffer[]        buffers    = SSLSocketChannel.createBuffers(engine.getSession());
    SSLHandshakeHandler handshaker = new SSLHandshakeHandler(socket, engine, buffers);

    try {
      engine.beginHandshake();
      if (!handshaker.doHandshake()) {
        System.out.println("Failed to handshake!");
        return null;
      }
      else {
        System.out.println("Handshake is done boie!");
      }
    }
    catch (IOException err) {
      System.err.println("IO Error while handshaking!\n - " + err.getMessage());
      return null;
    }

    return new SSLSocketChannel(socket, engine);
  }

  public static SSLSocketChannel newChannel(SocketChannel socket, String addr, int port) {
    SSLEngine engine = SSLEngineFactory.newEngine(addr, port);

    engine.setUseClientMode(false);
    if (engine == null) {
      return null;
    }
    ByteBuffer[]        buffers    = SSLSocketChannel.createBuffers(engine.getSession());
    SSLHandshakeHandler handshaker = new SSLHandshakeHandler(socket, engine, buffers);

    try {
      engine.beginHandshake();
      if (!handshaker.doHandshake()) {
        System.out.println("Failed to handshake!");
        return null;
      }
      else {
        System.out.println("Handshake is done boie!");
      }
    }
    catch (IOException err) {
      System.err.println("IO Error while handshaking!\n - " + err.getMessage());
      return null;
    }

    return new SSLSocketChannel(socket, engine);
  }

  private static SocketChannel openChannel(String addr, int port) {
    String err_msg;

    try {
      SocketChannel channel = SocketChannel.open();
      channel.configureBlocking(false);
      channel.connect(new InetSocketAddress(addr, port));
      channel.finishConnect();

      return channel;
    }
    catch (AlreadyConnectedException err) {
      err_msg = "Channel already connected!\n - " + err.getMessage();
    }
    catch (ConnectionPendingException err) {
      err_msg = "Non blocking already pending!\n - " + err.getMessage();
    }
    catch (ClosedChannelException err) {
      err_msg = "Channel is closed!\n - " + err.getMessage();
    }
    catch (UnresolvedAddressException err) {
      err_msg = "Given address not fully resolved!\n - " + err.getMessage();
    }
    catch (UnsupportedAddressTypeException err) {
      err_msg = "Type of given address is not supported!\n - " + err.getMessage();
    }
    catch (SecurityException err) {
      err_msg = "Security manager does not allow operation!\n - " + err.getMessage();
    }
    catch (IOException err) {
      err_msg = "Some I/O error occurred!\n - " + err.getMessage();
      err.printStackTrace();
    }

    System.err.println(err_msg);
    return null;
  }

  private static ByteBuffer[] createBuffers(SSLSession session) {
    ByteBuffer mine_app_data = ByteBuffer.allocate(session.getApplicationBufferSize());
    ByteBuffer mine_net_data = ByteBuffer.allocate(session.getPacketBufferSize());
    ByteBuffer peer_app_data = ByteBuffer.allocate(session.getApplicationBufferSize());
    ByteBuffer peer_net_data = ByteBuffer.allocate(session.getApplicationBufferSize());

    ByteBuffer[] buffers = { mine_app_data, mine_net_data, peer_app_data, peer_net_data };

    return buffers;
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
      this.app_buffer.clear();
      this.app_buffer.put(msg.getBytes());
      this.app_buffer.flip();
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
    while (this.app_buffer.hasRemaining()) {
      this.net_buffer.clear();
      SSLEngineResult res = this.engine.wrap(this.app_buffer, this.net_buffer);
      System.out.println(res);

      if (res.getStatus() == Status.OK) {
        this.net_buffer.flip();
        System.out.println("Sending net_buffer");
        while (this.net_buffer.hasRemaining()) {
          this.socket.write(this.net_buffer);
        }
        System.out.println("Sent net_buffer");
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

    this.net_buffer.clear();
    this.app_buffer.clear();
    try {
      num = this.socket.read(this.net_buffer);
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

    this.net_buffer.flip();
    SSLEngineResult res;
    try {
      while (this.net_buffer.hasRemaining()) {
        this.app_buffer.clear();
        res = this.engine.unwrap(this.net_buffer, this.app_buffer);

        if (res.getStatus() == SSLEngineResult.Status.OK) {
          this.app_buffer.flip();
          if (result == null) {
            result = new String(this.app_buffer.array());
          }
          else {
            result += new String(this.app_buffer.array());
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
