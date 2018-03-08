package controller;

import network.*;
import files.*;

abstract class Handler implements Runnable {
  public static ApplicationInfo app_info;
  Net_IO mc, mdr, mdb;

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
    else {
      System.out.println("TYPE = " + type);
      return null;
    }
  }
}
