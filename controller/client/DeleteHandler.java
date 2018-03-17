package controller.client;

import network.Net_IO;
import controller.Handler;
import controller.Pair;


import java.rmi.Remote;

class DeleteHandler extends Handler implements Remote {
  String file_name;
  Net_IO mc;

  void start(String f_name, Net_IO mc) {
    this.file_name = f_name;
    this.mc        = mc;
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
    System.out.println("GOT DELETE MSG!");
  }
}
