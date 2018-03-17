package controller;

import network.*;
import files.*;
import controller.ApplicationInfo;
import controller.Pair;
import controller.server.*;

import java.net.InetAddress;
import java.util.concurrent.ThreadPoolExecutor;

// TODO is message
public abstract class Handler implements Runnable {
  public InetAddress sender_addr;
  public int sender_port;

  protected Handler() {
    this.sender_addr = null;
    this.sender_port = 0;
  }

  public abstract void signal(String file_id);

  public abstract Pair<String, Handler> register();

  public abstract String signalType();

  public static Handler newHandler(PacketInfo packet) {
    String type = packet.getType();

    if (type.equals("PUTCHUNK")) {
      return new PutchunkHandler(packet, ApplicationInfo.getMC());
    }
    else if (type.equals("GETCHUNK")) {
      return new GetchunkHandler(packet, ApplicationInfo.getMDR());
    }
    else if (type.equals("DELETE")) {
      return new DeleteHandler(packet);
    }
    else if (type.equals("REMOVED")) {
      return new RemovedHandler(packet);
    }
    return null;
  }
}
