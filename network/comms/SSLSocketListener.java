package network.comms;

import java.util.Iterator;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.TimeUnit;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.spi.AbstractSelectableChannel;

import handlers.Handler;
import network.chord.Node;
import network.comms.sockets.SSLChannel;
import network.comms.sockets.SSLSocketChannel;
import network.comms.sockets.SSLServerSocketChannel;

// TODO multithreaded server
public class SSLSocketListener {
  private static final int MAX_SOCKETS = Node.BIT_NUMBER;
  private static final int POOL_SIZE   = MAX_SOCKETS * 3;

  private static ThreadPoolExecutor tasks;
  private ConcurrentHashMap<SocketChannel, PacketBuffer> channel_handlers;
  private Node myself;
  private Selector selector;

  public SSLSocketListener(Node node) {
    ArrayBlockingQueue arr = new ArrayBlockingQueue(POOL_SIZE, false);
    int cpu_number         = Runtime.getRuntime().availableProcessors();

    SSLSocketListener.tasks = new ThreadPoolExecutor(cpu_number, POOL_SIZE, 0, TimeUnit.SECONDS, (BlockingQueue)arr);
    this.myself             = node;
    this.selector           = SSLChannel.newSelector();
    this.channel_handlers   = new ConcurrentHashMap<SocketChannel, PacketBuffer>(MAX_SOCKETS);
  }

  /**
   * Listens to the sockets present in the selector
   */
  public void listen() throws IOException {
    String line;

    while (true) {
      if (this.selector.select() > 0) {
        Iterator<SelectionKey> keys_it = this.selector.selectedKeys().iterator();
        while (keys_it.hasNext()) {
          SelectionKey key = keys_it.next();
          if (!this.handleKey(key)) {
            return;
          }
          keys_it.remove();
        }
      }
    }
  }

  public boolean waitForRead(PacketBuffer builder) {
    this.channel_handlers.put(builder.getChannel(), builder);
    SelectionKey key = this.registerSocket(builder.getChannel(), SelectionKey.OP_READ);
    return key != null;
  }

  public boolean waitForAccept(AbstractSelectableChannel socket) {
    return this.registerSocket(socket, SelectionKey.OP_ACCEPT) != null;
  }

  private boolean handleKey(SelectionKey key) {
    if (key.isAcceptable() && key.isValid()) {
      System.out.println("Accepting!");
      ServerSocketChannel server_socket = (ServerSocketChannel)key.channel();
      return this.acceptKey((ServerSocketChannel)key.channel());
    }
    else if (key.isReadable()) {
      return this.readKey(this.channel_handlers.get(key.channel()));
    }
    else {
      System.out.println("Some other state");
      return false;
    }
  }

  private boolean acceptKey(ServerSocketChannel server_channel) {
    try {
      SocketChannel s_channel = server_channel.accept();
      if (s_channel != null) {
        SSLSocketChannel socket = SSLSocketChannel.newChannel(s_channel, false);
        PacketBuffer     buffer = new PacketBuffer(socket);

        this.waitForRead(buffer);
        return true;
      }
      return false;
    }
    catch (Exception err) {
      err.printStackTrace();
      System.err.println("Connection not accepted!\n - " + err.getMessage());
    }
    return false;
  }

  private boolean readKey(PacketBuffer builder) {
    SSLSocketListener.tasks.execute((Runnable)builder);
    return true;
  }

  private SelectionKey registerSocket(AbstractSelectableChannel socket, int ops) {
    String err_msg;

    try {
      socket.configureBlocking(false);
      return socket.register(this.selector, ops);
    }
    catch (ClosedChannelException err) {
      err_msg = "The channel is closed!\n - " + err.getMessage();
    }
    catch (ClosedSelectorException err) {
      err_msg = "The selector is closed!\n - " + err.getMessage();
    }
    catch (IllegalBlockingModeException err) {
      err_msg = "The socket trying to register is in blocking-mode!\n - " + err.getMessage();
    }
    catch (IllegalSelectorException err) {
      err.printStackTrace();
      err_msg  = "Channel not created with same provider!\n - " + err.getMessage();
      err_msg += "\n" + err.getCause() + err.toString();
    }
    catch (IllegalArgumentException err) {
      err_msg = "Bit in ops is not supported!\n - " + err.getMessage();
    }
    catch (IOException err) {
      err_msg = "I/O error while configuring blocking!\n - " + err.getMessage();
    }

    System.err.println(err_msg);
    return null;
  }

  private String msgType(String msg) {
    String type     = "";
    int    msg_size = msg.length();

    for (int i = 0; i < msg_size; i++) {
      if (msg.charAt(i) != ' ' && msg.charAt(i) != '\n' && msg.charAt(i) != '\r') {
        type += msg.charAt(i);
      }
      else {
        return type;
      }
    }
    return type;
  }
}
