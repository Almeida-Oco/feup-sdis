package controller.client;

import network.Net_IO;
import controller.Handler;
import controller.listener.Listener;
import controller.Pair;

import java.rmi.Remote;

class RestoreHandler extends Handler implements Remote {
  String file_name;
  Listener mc, mdr;

  void start(String file_name, Listener mc, Listener mdr) {
    this.file_name = file_name;
    this.mc        = mc;
    this.mdr       = mdr;
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
    System.out.println("GOT RESTORE MSG!");
  }
}
