package controller;

import network.*;
import controller.handler.Handler;
import java.util.concurrent.ThreadPoolExecutor;

public class MDBListener implements Runnable {
  Net_IO mc, mdr, mdb;
  ThreadPoolExecutor task_queue;

  MDBListener(Net_IO mc, Net_IO mdr, Net_IO mdb, ThreadPoolExecutor queue) {
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
        System.out.println("LOG: mdb::recvMsg() -> " + packet.getType());
        Handler handle = Handler.newHandler(packet, mc, mdr, mdb, this.task_queue);

        this.task_queue.execute(handle);

        if (packet.getType().equals("GETCHUNK")) {
          try {
            handle.join(); //Give newly generated thread control over the mdb channel
          }
          catch (InterruptedException err) {
            System.err.println("Failed to join thread!\n - " + err.getMessage());
          }
        }
      }
    } while (txt == null || !txt.equals("EXIT"));

    this.task_queue.shutdown();
  }
}
