package controller.server;

import files.*;
import network.PacketInfo;

class DeleteHandler extends Handler {
  String file_id;

  DeleteHandler(PacketInfo packet) {
    super();
    this.file_id = packet.getFileID();
  }

  public void signal(PacketInfo packet) {
  }

  public void run() {
    File_IO.eraseFile(this.file_id);
  }
}
