package controller;

import network.*;
import controller.handler.Handler;
import java.util.concurrent.ThreadPoolExecutor;

public class NetworkListener implements Runnable {
  Net_IO mc, mdr, mdb;
  ThreadPoolExecutor task_queue;

  NetworkListener(Net_IO mc, Net_IO mdr, Net_IO mdb, ThreadPoolExecutor queue) {
    this.mc         = mc;
    this.mdr        = mdr;
    this.mdb        = mdb;
    this.task_queue = queue;
    //Start threads equal to number of cores - 1. Save 1 for the NetworkListener
    for (int i = 1; i < Runtime.getRuntime().availableProcessors(); i++) {
      this.task_queue.prestartCoreThread();
    }
  }

  public void run() {
    String     txt = null;
    PacketInfo packet;

    System.out.println("Listening...");
    do {
      if ((packet = this.mc.recvMsg()) != null) {
        System.out.println("LOG: recvMsg() -> " + packet.getType());
        this.task_queue.execute(Handler.newHandler(packet, mc, mdr, mdb));
      }
    } while (txt == null || !txt.equals("EXIT"));

    this.task_queue.shutdown();
  }
}
