package controller.server;

import files.*;
import network.PacketInfo;
import controller.Pair;
import controller.Handler;

public class DeleteHandler extends Handler {
  String file_id;

  public DeleteHandler(PacketInfo packet) {
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
    if (File_IO.eraseFileChunks(this.file_id)) {
      System.out.println("Erased chunks of file '" + this.file_id + "'");
    }
  }
}
