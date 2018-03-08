package controller.handler;

import files.*;
import network.*;

class StoredHandler extends Handler {
  String file_id;
  int chunk_n;
  StoredHandler(PacketInfo packet, Net_IO mc, Net_IO mdr, Net_IO mdb) {
    super(mc, mdr, mdb);
    this.file_id = packet.getFileID();
    this.chunk_n = packet.getChunkN();
  }

  public void run() {
    File_IO.incReplication(this.file_id, this.chunk_n);
  }
}
