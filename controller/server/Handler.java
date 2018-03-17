package controller.server;

import network.*;
import files.*;
import controller.ApplicationInfo;

import java.net.InetAddress;
import java.util.concurrent.ThreadPoolExecutor;

// TODO is message
public abstract class Handler extends Thread {
  Net_IO mc, mdr, mdb;

  InetAddress sender_addr;
  int sender_port;

  protected Handler(Net_IO mc, Net_IO mdr, Net_IO mdb) {
    this.mc  = mc;
    this.mdr = mdr;
    this.mdb = mdb;
  }

  protected Handler() {
    this.mc          = null;
    this.mdr         = null;
    this.mdb         = null;
    this.sender_addr = null;
    this.sender_port = 0;
  }

  public abstract void signal(String file_id);

  public abstract Pair<String, Handler> register();

  public abstract String signalType();

  public static Handler newHandler(PacketInfo packet, Net_IO mc, Net_IO mdr, Net_IO mdb) {
    String type = packet.getType();

    if (type.equals("PUTCHUNK")) {
      return new PutchunkHandler(packet, mc, mdr, mdb);
    }
    else if (type.equals("GETCHUNK")) {
      return new GetchunkHandler(packet, mc, mdr, mdb);
    }
    else if (type.equals("DELETE")) {
      return new DeleteHandler(packet, mc, mdr, mdb);
    }
    else if (type.equals("REMOVED")) {
      return new RemovedHandler(packet, mc, mdr, mdb);
    }
    return null;
  }
}
