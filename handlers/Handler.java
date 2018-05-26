package handlers;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.concurrent.ThreadPoolExecutor;

import network.comms.sockets.SSLSocketChannel;

public abstract class Handler implements Runnable {
  private static final LinkedHashMap<String, Function<SSLSocketChannel, Handler> > generator;
  public static final String NEW_PEER    = "NEW_PEER";
  public static final String CODE        = "CODE";
  public static final String LEAVING     = "LEAVING";
  public static final String ALIVE       = "ALIVE?";
  public static final String PREDECESSOR = "PREDECESSOR";

  static {
    generator = new LinkedHashMap<String, Function<SSLSocketChannel, Handler> >(5);
    generator.put(NEW_PEER, (rem_peer)->{
      System.out.println("Got new peer");
      return null;
    });
    generator.put(CODE, (rem_peer)->{
      System.out.println("Got a code request");
      return null;
    });
    generator.put(LEAVING, (rem_peer)->{
      System.out.println("Peer is leaving");
      return null;
    });
    generator.put(ALIVE, (rem_peer)->{
      System.out.println("Peer requested heartbeat");
      return null;
    });
    generator.put(PREDECESSOR, (rem_peer)->{
      System.out.println("Requested predecessor");
      return null;
    });
  }

  public abstract void signal(String msg, SSLSocketChannel remote_peer);

  public static Handler newHandler(String msg_type, SSLSocketChannel remote_peer) {
    return generator.get(msg_type).apply(remote_peer);
  }
}
