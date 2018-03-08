package controller.handler;

import network.*;
import files.*;
import java.net.InetAddress;

class PutchunkHandler extends Handler {
  byte version;
  String file_id;
  int chunk_n;
  String data;

  PutchunkHandler(PacketInfo packet, Net_IO mc, Net_IO mdr, Net_IO mdb) {
    super(mc, mdr, mdb);
    this.version     = packet.getVersion();
    this.file_id     = packet.getFileID();
    this.chunk_n     = packet.getChunkN();
    this.data        = packet.getData();
    this.sender_addr = packet.getAddress();
    this.sender_port = packet.getPort();
  }

  public void run() {
    File_IO.storeFile(this.file_id + this.chunk_n, data.getBytes());
    PacketInfo packet = new PacketInfo(this.sender_addr, this.sender_port);

    packet.setType("STORED");
    packet.setVersion(this.version);
    packet.setFileID(this.file_id);
    packet.setChunkN(this.chunk_n);
    packet.setData(this.data);

    mc.sendMsg(packet);
  }
}
