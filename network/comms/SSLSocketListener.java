package network.comms;

import java.util.LinkedHashMap;
import java.nio.channels.Selector;
import javax.net.ssl.SSLServerSocket;
import java.nio.channels.SelectionKey;
import javax.net.ssl.SSLServerSocketFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.IllegalBlockingModeException;

import handlers.Handler;
import network.chord.Node;
import network.comms.sockets.SSLSocketChannel;

class SSLSocketListener {
  private static ThreadPoolExecutor tasks;
  private Node myself;
  private Selector selector;

  /**
   * Creates a new connection listener
   * @param handlers [join_handler, check_handler, code_handler, add_handler]
   */
  SSLSocketListener(Node node) {
    this.myself   = node;
    this.selector = new Selector();
  }

  /** Listens to the given connection */
  public void listen(SSLSocketChannel conn) {
    String line;

    while (true) {
      if ((line = conn.recvMsg()) != null) {
        Handler handler = Handler.newHandler(this.msgType(line), null);
        // Runnable handler = this.handlers.get(this.msgType(line));
      }
    }
  }

  public boolean listenToSocket(SSLSocketChannel socket) {
    socket.register(this.selector, SelectionKey.OP_READ);
  }

  private boolean registerSocket(SSLSocketChannel socket, int ops) {
    String err_msg;

    try {
      return socket.register(this.selector, ops) != null;
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
