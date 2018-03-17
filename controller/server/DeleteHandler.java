import files.*;

class DeleteHandler extends Handler {
  String file_id;

  DeleteHandler(PacketInfo packet) {
    this.file_id = packet.getFileID();
  }

  public void signal(PacketInfo packet) {
  }

  public void run() {
    File_IO.eraseFile(this.file_id);
  }
}
