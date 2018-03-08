package controller.handler;

import network.*;
import files.*;
import controller.ApplicationInfo;
import java.net.InetAddress;

// TODO is message
public abstract class Handler implements Runnable {
  public static ApplicationInfo app_info;
  Net_IO mc, mdr, mdb;

  InetAddress sender_addr;
  int sender_port;

  protected Handler(Net_IO mc, Net_IO mdr, Net_IO mdb) {
    this.mc  = mc;
    this.mdr = mdr;
    this.mdb = mdb;
  }

  public static Handler newHandler(PacketInfo packet, Net_IO mc, Net_IO mdr, Net_IO mdb) {
    String type = packet.getType();

    if (type.equals("PUTCHUNK")) {
      return new PutchunkHandler(packet, mc, mdr, mdb);
    }
    else if (type.equals("GETCHUNK")) {
      return new GetchunkHandler(packet, mc, mdr, mdb);
    }
    else if (type.equals("STORED")) {
      return new StoredHandler(packet);
    }
    else {
      System.err.println("Unknown type '" + type + "'");
      return null;
    }
  }
}
