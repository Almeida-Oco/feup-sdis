package controller;

import network.*;
import controller.handler.Handler;
import java.util.concurrent.ThreadPoolExecutor;

public class MCListener implements Runnable {
  Net_IO mc, mdr, mdb;
  ThreadPoolExecutor task_queue;

  MCListener(Net_IO mc, Net_IO mdr, Net_IO mdb, ThreadPoolExecutor queue) {
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
        this.task_queue.execute(Handler.newHandler(packet, mc, mdr, mdb, this.task_queue));
      }
    } while (txt == null || !txt.equals("EXIT"));

    this.task_queue.shutdown();
  }
}
