package controller.client;

import network.*;
import files.File_IO;
import controller.Pair;
import files.FileChunk;
import controller.Handler;
import controller.listener.Listener;

import java.rmi.Remote;
import java.util.Vector;


class ReclaimHandler extends Handler implements Remote {
  int space;
  Listener mc, mdb;

  void start(int space, Listener mc) {
    this.space = space;
    this.mc    = mc;
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
    Vector<Pair<String, FileChunk> > chunks = File_IO.reclaimSpace(this.space);
    for (Pair<String, FileChunk> pair : chunks) {
      System.out.println("Removing " + pair.first() + "#" + pair.second().getChunkN());
    }
  }
}
