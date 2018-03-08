package controller.handler;

class StoredHandler extends Handler {
  String file_id;
  int chunk_n;
  StoredHandler(PacketInfo packet) {
    this.file_id = packet.getFileID();
    this.chunk_n = packet.getChunkN();
  }

  public void run() {
    File_IO.incReplication(this.file_id, this.chunk_n);
  }
}
