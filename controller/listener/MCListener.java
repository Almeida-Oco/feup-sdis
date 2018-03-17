package controller.listener;

import network.*;
import controller.server.Handler;
import java.util.concurrent.ThreadPoolExecutor;

public class MCListener implements Runnable {
  Net_IO mc, mdr, mdb;
  ThreadPoolExecutor task_queue;

  public MCListener(Net_IO mc, Net_IO mdb, Net_IO mdr, ThreadPoolExecutor queue) {
    this.mc         = mc;
    this.mdr        = mdr;
    this.mdb        = mdb;
    this.task_queue = queue;
  }

  public void run() {
    String     txt = null;
    PacketInfo packet;

    System.out.println("Listening...");
    do {
      if ((packet = this.mc.recvMsg()) != null) {
        System.out.println("LOG: mc::recvMsg() -> " + packet.getType());
        Handler task = Handler.newHandler(packet, mc, mdr, mdb);
        this.task_queue.execute(task);
      }
    } while (txt == null || !txt.equals("EXIT"));

    this.task_queue.shutdown();
  }
}
