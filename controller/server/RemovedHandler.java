package controller.server;

import files.*;
import network.PacketInfo;

class RemovedHandler extends Handler {
  String file_id;

  RemovedHandler(PacketInfo packet) {
    super();
    this.file_id = packet.getFileID();
  }

  @Override
  public void signal(String file_id, int chunk_n) {
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
    File_IO.eraseFile(this.file_id);
  }
}
