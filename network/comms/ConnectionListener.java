package network.comms;

import java.util.LinkedHashMap;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


class ConnectionListener {
  private LinkedHashMap<String, Runnable> handlers;
  private static final String JOIN  = "NEW_PEER";
  private static final String CHECK = "ALIVE?";
  private static final String CODE  = "CODE";
  private static final String ADD   = "ADD_PEER";

  /**
   * Creates a new connection listener
   * @param handlers [join_handler, check_handler, code_handler, add_handler]
   */
  ConnectionListener(Runnable[] handlers) {
    this.handlers = new LinkedHashMap<String, Runnable>(5);
    this.handlers.put(JOIN, handlers[0]);
    this.handlers.put(CHECK, handlers[1]);
    this.handlers.put(CODE, handlers[2]);
    this.handlers.put(ADD, handlers[3]);
  }

  /** Listens to the given connection */
  public void listen(Connection conn) {
    String line;

    while (true) {
      if ((line = conn.getLine()) != null) {
        Runnable handler = this.handlers.get(this.msgType(line));
      }
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
