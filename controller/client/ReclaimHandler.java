package controller.client;

import java.rmi.Remote;
import network.Net_IO;

class ReclaimHandler implements Remote, Runnable {
  int space;
  Net_IO mc, mdb;

  void start(int space, Net_IO mc, Net_IO mdb) {
    this.space = space;
    this.mc    = mc;
    this.mdb   = mdb;
    this.run();
  }

  public void run() {
    System.out.println("GOT RECLAIM MSG!");
  }
}
