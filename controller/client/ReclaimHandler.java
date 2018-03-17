package controller.client;

import network.Net_IO;
import controller.Handler;
import controller.Pair;


import java.rmi.Remote;

class ReclaimHandler extends Handler implements Remote {
  int space;
  Net_IO mc, mdb;

  void start(int space, Net_IO mc, Net_IO mdb) {
    this.space = space;
    this.mc    = mc;
    this.mdb   = mdb;
    this.run();
  }

  @Override
  public void signal(String file_id) {
  }

  @Override
  public Pair<String, Handler> register() {
    return null;
  }

  @Override
  public String signalType() {
    return null;
  }

  @Override
  public void run() {
    System.out.println("GOT RECLAIM MSG!");
  }
}
