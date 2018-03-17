package controller.client;

import controller.Handler;
import controller.Pair;


import java.rmi.Remote;

class StateHandler extends Handler implements Remote {
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
    System.out.println("GOT STATE MSG!");
  }
}
