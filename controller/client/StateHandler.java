package controller.client;

import files.*;
import cli.User_IO;
import controller.Pair;
import controller.Handler;
import network.PacketInfo;

import java.rmi.Remote;

/**
 * Handler for the State instruction from the client
 * @author Gonçalo Moreno
 * @author João Almeida
 */
class StateHandler extends Handler implements Remote {
  @Override
  public void signal(PacketInfo packet) {
  }

  @Override
  public void run() {
    User_IO.printState(FileHandler.getBackedUpTable(), FileHandler.getChunksTable(),
        FileHandler.getMaxSpace(), FileHandler.getUsedSpace());
  }
}
