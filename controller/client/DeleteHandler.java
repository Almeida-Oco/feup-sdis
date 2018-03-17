package controller.client;

import network.*;
import controller.Handler;
import controller.listener.Listener;
import controller.Pair;


import java.rmi.Remote;

class DeleteHandler extends Handler implements Remote {
  String file_name;
  Listener mc;

  void start(String f_name, Listener mc) {
    this.file_name = f_name;
    this.mc        = mc;
    this.run();
  }

  @Override
  public void signal(PacketInfo packet) {
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
