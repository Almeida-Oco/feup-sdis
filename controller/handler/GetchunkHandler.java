package controller.handler;

import network.*;
import files.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

class GetchunkHandler extends Handler {
  byte version;
  String file_id;
  int chunk_n;

  GetchunkHandler(PacketInfo packet, Net_IO mc, Net_IO mdr, Net_IO mdb) {
    super(mc, mdr, mdb);
    this.version     = packet.getVersion();
    this.file_id     = packet.getFileID();
    this.chunk_n     = packet.getChunkN();
    this.sender_addr = packet.getAddress();
    this.sender_port = packet.getPort();
  }

  public void run() {
    FileChunk chunk = File_IO.getChunk(this.file_id + this.chunk_n);

    if (chunk != null) {
      PacketInfo packet = new PacketInfo(this.sender_addr, this.sender_port);

      packet.setType("CHUNK");
      packet.setVersion(this.version);
      packet.setFileID(this.file_id);
      packet.setChunkN(this.chunk_n);
      packet.setData(new String(chunk.getData(), StandardCharsets.US_ASCII));
      Random rand = new Random();

      Thread.sleep(rand.nextInt(401)); //TODO use ScheduledExecutorService?

      mdr.sendMsg(packet);
    }
  }
}
