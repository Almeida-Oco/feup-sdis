package controller.client;

import java.rmi.Remote;
import network.Net_IO;

class RestoreHandler implements Remote, Runnable {
  String file_name;
  Net_IO mc, mdr;

  void start(String file_name, Net_IO mc, Net_IO mdr) {
    this.file_name = file_name;
    this.mc        = mc;
    this.mdr       = mdr;
    this.run();
  }

  public void run() {
    System.out.println("GOT RESTORE MSG!");
  }
}
