package controller;

import network.*;
import java.util.concurrent.ThreadPoolExecutor;

class InputNetworkThread implements Runnable {
  Net_IO mc, mdr, mdb;
  ThreadPoolExecutor task_queue;

  InputNetworkThread(Net_IO mc, Net_IO mdr, Net_IO mdb, ThreadPoolExecutor queue) {
    this.mc         = mc;
    this.mdr        = mdr;
    this.mdb        = mdb;
    this.task_queue = queue;
  }

  public void run() {
    String     txt = null;
    PacketInfo packet;

    do {
      if ((packet = this.mc.recvMsg()) != null) {
        this.task_queue.execute(Handler.newHandler(packet, mc, mdr, mdb));
      }
    } while (txt == null || !txt.equals("EXIT"));

    this.task_queue.shutdown();
  }
}
