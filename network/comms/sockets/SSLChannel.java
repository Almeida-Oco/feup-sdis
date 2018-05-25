package network.comms.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.nio.channels.Selector;
import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.lang.SecurityException;
import javax.net.ssl.SSLEngineResult;
import java.nio.channels.SelectionKey;
import java.util.concurrent.Executors;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLEngineResult.Status;
import java.nio.channels.spi.SelectorProvider;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.ConnectionPendingException;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.UnsupportedAddressTypeException;

public class SSLChannel extends AbstractSelectableChannel {
  protected static SelectorProvider provider = SelectorProvider.provider();
  protected SocketChannel socket;
  protected SSLEngine engine;
  protected ByteBuffer my_app_data, my_net_data, peer_app_data, peer_net_data;

  protected SSLChannel(SocketChannel socket, SSLEngine engine, SelectorProvider provider) {
    super(provider);
    SSLSession s = engine.getSession();
    this.socket = socket;
    this.engine = engine;

    int app_size = s.getApplicationBufferSize(),
        net_size = s.getPacketBufferSize();
    this.my_app_data   = ByteBuffer.allocate(app_size);
    this.my_net_data   = ByteBuffer.allocate(net_size);
    this.peer_app_data = ByteBuffer.allocate(app_size);
    this.peer_net_data = ByteBuffer.allocate(net_size);
  }

  protected boolean doHandshake() throws IOException {
    SSLEngineResult engine_res = null;
    HandshakeStatus shake_status;

    this.engine.beginHandshake();
    while (!this.handshakeFinished((shake_status = this.engine.getHandshakeStatus()))) {
      System.out.println("STATUS = " + shake_status);

      // try {
      //   Thread.sleep(500, 0);
      // }
      // catch (InterruptedException err) {}

      if (shake_status == HandshakeStatus.NEED_UNWRAP) {
        int bytes_read;
        if ((bytes_read = this.socket.read(this.peer_net_data)) < 0) {
          if (this.engine.isInboundDone() && this.engine.isOutboundDone()) {
            System.err.println("Read < 0 data");
            return false;
          }

          try {
            this.engine.closeInbound();
            this.engine.closeOutbound();
          }
          catch (SSLException err) {
            System.err.println("Engine forced to close inbound!\n - " + err.getMessage());
          }
        }
        else {
          try {
            this.peer_net_data.flip();
            engine_res = engine.unwrap(this.peer_net_data, this.peer_app_data);
            this.peer_net_data.compact();
          }
          catch (SSLException err) {
            System.err.println("Problem encountered while processing data!\n - " + err.getMessage());
            err.printStackTrace();
            this.engine.closeOutbound();
          }

          Status status = engine_res.getStatus();
          if (status == Status.OK) {
            System.out.println("OK");
          }
          else if (status == Status.BUFFER_OVERFLOW) {
            System.out.println("  OVERFLOW");
            this.enlargeBuffer(this.peer_app_data, this.engine.getSession().getApplicationBufferSize());
          }
          else if (status == Status.BUFFER_UNDERFLOW) {
            System.out.println("  UNDERFLOW");
            this.peer_net_data.flip();
            this.socket.read(this.peer_net_data);
            engine_res = engine.unwrap(this.peer_net_data, this.peer_app_data);
            this.peer_net_data.compact();
          }
          else if (status == Status.CLOSED) {
            System.out.println("CLOSED!");
            System.exit(2);
          }
        }
      }
      else if (shake_status == HandshakeStatus.NEED_WRAP) {
        this.my_net_data.clear();

        try {
          engine_res = this.engine.wrap(this.my_app_data, this.my_net_data);
          this.my_net_data.flip();

          while (this.socket.write(this.my_net_data) > 0) {
          }
          Status status = engine_res.getStatus();
          if (status == Status.OK) {
            System.out.println("  OK");
          }
          else if (status == Status.BUFFER_OVERFLOW) {
            System.out.println("  OBVERFLOW");
            this.socket.write(this.my_net_data);
            this.my_net_data.compact();
          }
          else if (status == Status.CLOSED) {
            System.out.println("CLOSED!");
            System.exit(2);
          }
        }
        catch (SSLException err) {
          System.err.println("Problem encountered while processing data!\n - " + err.getMessage());
        }
      }
      else if (shake_status == HandshakeStatus.NEED_TASK) {
        Runnable task;
        while ((task = this.engine.getDelegatedTask()) != null) {
          task.run();
        }
      }
      else {
        System.err.println("Invalid SSL status: " + shake_status);
        return false;
      }
    }

    System.err.println("End of while");
    return this.handshakeFinished(shake_status);
  }

  protected static SocketChannel openChannel(String addr, int port) {
    String err_msg;

    try {
      SocketChannel channel = SSLChannel.provider.openSocketChannel();
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

  private ByteBuffer shrinkBuffer(ByteBuffer buffer) {
    if (this.engine.getSession().getPacketBufferSize() < buffer.limit()) {
      return buffer;
    }
    else {
      ByteBuffer replacement = this.enlargeBuffer(buffer, this.engine.getSession().getPacketBufferSize());
      buffer.flip();
      replacement.put(buffer);
      return replacement;
    }
  }

  private boolean handshakeFinished(HandshakeStatus status) {
    return status == HandshakeStatus.FINISHED || status == HandshakeStatus.NOT_HANDSHAKING;
  }

  private ByteBuffer enlargeBuffer(ByteBuffer buffer, int proposed_size) {
    if (proposed_size > buffer.capacity()) {
      buffer = ByteBuffer.allocate(proposed_size);
    }
    else {
      buffer = ByteBuffer.allocate(buffer.capacity() * 2);
    }

    return buffer;
  }

  public static Selector newSelector() {
    try {
      return (Selector)SSLChannel.provider.openSelector();
    }
    catch (IOException err) {
      System.err.println("Failed to open selector!\n - " + err.getMessage());
      return null;
    }
  }

  @Override
  protected void implCloseSelectableChannel() {
  }

  @Override
  protected void implConfigureBlocking(boolean block) {
  }

  @Override
  public int validOps() {
    return SelectionKey.OP_ACCEPT | SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE;
  }
}
