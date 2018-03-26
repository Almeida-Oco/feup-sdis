package controller.server;

import files.*;
import controller.Pair;
import controller.Handler;
import network.PacketInfo;

public class RemovedHandler extends Handler {
  String file_id;

  public RemovedHandler(PacketInfo packet) {
    super();
    this.file_id = packet.getFileID();
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

  public void run() {
    System.out.println("GOT REMOVED MSG");
    // File_IO.eraseFile(this.file_id);
  }
}
