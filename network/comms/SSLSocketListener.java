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
  private Node myself;
  private static Selector selector;

  static {
    selector = SSLChannel.newSelector();
  }

  public SSLSocketListener(Node node) {
    ArrayBlockingQueue<Runnable> arr = new ArrayBlockingQueue<Runnable>(POOL_SIZE, false);
    int cpu_number = Runtime.getRuntime().availableProcessors();

    SSLSocketListener.tasks = new ThreadPoolExecutor(cpu_number, POOL_SIZE, (long)0, TimeUnit.SECONDS, arr);
    this.myself             = node;
  }

  /**
   * Listens to the sockets present in the selector
   */
  public void listen() throws IOException {
    String line;

    while (true) {
      if (selector.select(50) > 0) {
        Iterator<SelectionKey> keys_it = selector.selectedKeys().iterator();
        while (keys_it.hasNext()) {
          SelectionKey key = keys_it.next();
          if (!key.isValid() || !this.handleKey(key)) {
            key.cancel();
          }
          keys_it.remove();
        }
      }
    }
  }

  public static boolean waitForRead(PacketChannel builder) {
    SocketChannel channel = builder.getChannel();

    if (!channel.isRegistered()) {
      SelectionKey key = registerSocket(builder.getChannel(), SelectionKey.OP_READ);

      if (key != null) {
        key.attach(builder);
        return true;
      }
      return false;
    }
    else {
      SelectionKey key = channel.keyFor(selector);
      key.interestOps(key.interestOps() | SelectionKey.OP_READ);
      return true;
    }
  }

  public static boolean waitForWrite(PacketChannel builder) {
    SocketChannel channel = builder.getChannel();

    if (!channel.isRegistered()) {
      SelectionKey key = registerSocket(builder.getChannel(), SelectionKey.OP_WRITE);

      if (key != null) {
        key.attach(builder);
        return true;
      }
      return false;
    }
    else {
      SelectionKey key = channel.keyFor(selector);
      key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
      return true;
    }
  }

  public static boolean waitForAccept(AbstractSelectableChannel socket) {
    return registerSocket(socket, SelectionKey.OP_ACCEPT) != null;
  }

  private boolean handleKey(SelectionKey key) {
    if (key.isAcceptable() && key.isValid()) {
      ServerSocketChannel server_socket = (ServerSocketChannel)key.channel();
      return this.acceptKey((ServerSocketChannel)key.channel());
    }
    else if (key.isReadable()) {
      System.out.println("GOT A READABLE KEY!");
      boolean ret = this.readKey((PacketChannel)key.attachment());
      return ret;
    }
    else if (key.isWritable()) {
      this.writeKey((PacketChannel)key.attachment());
      return true;
    }
    else {
      System.out.println("Some other state");
      return false;
    }
  }

  private boolean acceptKey(ServerSocketChannel server_channel) {
    try {
      System.out.println("Accepting");
      SocketChannel s_channel = server_channel.accept();
      if (s_channel != null) {
        SSLSocketChannel socket = SSLSocketChannel.newChannel(s_channel, false);
        PacketChannel    buffer = new PacketChannel(socket);

        waitForRead(buffer);
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

  private boolean readKey(PacketChannel builder) {
    if (builder.isConnected()) {
      SSLSocketListener.tasks.execute((Runnable)builder);
      return true;
    }
    return false;
  }

  private boolean writeKey(PacketChannel builder) {
    SSLSocketListener.tasks.execute(()->builder.send());
    return true;
  }

  private static SelectionKey registerSocket(AbstractSelectableChannel socket, int ops) {
    String err_msg;

    try {
      socket.configureBlocking(false);
      selector.wakeup();
      SelectionKey key = socket.register(selector, ops);
      return key;
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
      err_msg = "Channel not created with same provider!\n - " + err.getMessage();
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

  public static void unregisterChannel(PacketChannel channel) {
    SelectionKey key = channel.getChannel().keyFor(selector);

    key.cancel();
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

  public static void shutdown() {
    try {
      selector.close();
      tasks.shutdown();
    }
    catch (Exception err) {
      System.err.println("Failed to shutdown SSLSocketListener!\n - " + err.getMessage());
    }
  }
}
