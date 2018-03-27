package controller.client;

import files.*;
import cli.User_IO;
import controller.Pair;
import controller.Handler;
import network.PacketInfo;

import java.rmi.Remote;

class StateHandler extends Handler implements Remote {
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
    User_IO.printState(File_IO.getBackedUpTable(), File_IO.getChunksTable());
  }
}
