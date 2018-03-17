package controller.server;

import network.PacketInfo;
import network.Net_IO;
import files.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

class GetchunkHandler extends Handler {
  boolean got_chunk = false;
  byte version;
  String file_id;
  int chunk_n;
  PacketInfo curr_packet;

  PacketInfo listen() {
    return PacketInfo.packetWith("CHUNK", this.file_id, this.chunk_n);
  }

  //TODO should I just store the packet then initialize?
  // How much overhead is added with these initializations?
  GetchunkHandler(PacketInfo packet, Net_IO mc, Net_IO mdr, Net_IO mdb) {
    super(mc, mdr, mdb);
    this.version     = packet.getVersion();
    this.file_id     = packet.getFileID();
    this.chunk_n     = packet.getChunkN();
    this.curr_packet = packet;
  }

  public void run() {
    FileChunk chunk = File_IO.getChunk(this.file_id, this.chunk_n);

    if (chunk != null) {
      PacketInfo packet = new PacketInfo(this.sender_addr, this.sender_port);

      packet.setType("CHUNK");
      packet.setVersion(this.version);
      packet.setFileID(this.file_id);
      packet.setChunkN(this.chunk_n);
      packet.setData(new String(chunk.getData(), StandardCharsets.US_ASCII));
      Random rand = new Random();

      // Thread.sleep(rand.nextInt(401)); //TODO use ScheduledExecutorService?
      synchronized (this) {
        if (!this.got_chunk) {
          mdr.sendMsg(packet);
        }
      }
    }
  }
}
