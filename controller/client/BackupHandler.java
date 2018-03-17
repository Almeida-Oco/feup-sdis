package controller.client;

import java.rmi.Remote;
import network.*;
import files.*;

class BackupHandler implements Remote, Runnable {
  String file_name;
  int rep_degree;
  Net_IO mc, mdb;

  void start(String f_name, int rep_degree, Net_IO mc, Net_IO mdb) {
    this.file_name  = f_name;
    this.rep_degree = rep_degree;
    this.mc         = mc;
    this.mdb        = mdb;
    this.run();
  }

  public void run() {
    FileInfo file = File_IO.readFile(this.file_name, this.rep_degree);
    String   id   = file.getID();

    for (FileChunk chunk : file.getChunks()) {
      int        n      = chunk.getChunkN();
      PacketInfo packet = PacketInfo.packetWith("PUTCHUNK", id + n, n);
      packet.setData(chunk.getData());
      packet.setRDegree(this.rep_degree);
      packet.setAddress(this.mdb.getAddr());
      packet.setPort(this.mdb.getPort());

      this.mdb.sendMsg(packet);
    }

    System.out.println("BACKUP");
  }

  private int countConfirmations(String file_id, int chunk_n) {
    return 0;
  }
}