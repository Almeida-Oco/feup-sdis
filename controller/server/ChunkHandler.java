package controller.server;

import network.PacketInfo;
import files.*;

class ChunkHandler extends Handler {
  String file_id;
  int chunk_n;
  String data;
  int data_size;


  ChunkHandler(PacketInfo packet) {
    this.file_id = packet.getFileID();
    this.chunk_n = packet.getChunk();
    this.data    = packet.getData();
  }

  PacketInfo listen(PacketInfo packet) {
    return null;
  }

  public void run() {
    if (!File_IO.fileExists(this.file_id + this.chunk_n)) {
      File_IO.storeChunk(this.file_id,
                         new FileChunk(this.data.getBytes(), this.data.length(), this.chunk_n));
    }
    else {
      System.out.println("Duplicate CHUNK message!");
    }
  }
}
