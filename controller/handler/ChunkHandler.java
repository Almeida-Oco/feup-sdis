package controller.handler;

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

  public void run() {
    if (!File_IO.fileExists(this.file_id + this.chunk_n)) {
      File_IO.storeChunk(this.file_id,
                         new FileChunk(this.data.getBytes(), this.data.length(), this.chunk_n));
    }
    System.out.println("Duplicate CHUNK message!");
  }
}
