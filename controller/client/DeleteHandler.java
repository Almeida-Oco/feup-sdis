package controller.client;

import java.rmi.Remote;
import network.Net_IO;

class DeleteHandler implements Remote, Runnable {
  String file_name;
  Net_IO mc;

  void start(String f_name, Net_IO mc) {
    this.file_name = f_name;
    this.mc        = mc;
    this.run();
  }

  public void run() {
    System.out.println("GOT DELETE MSG!");
  }
}
