package controller;

import network.*;
import files.*;
import java.net.InetAddress;

class PutchunkHandler extends Handler {
  byte version;
  String file_id;
  int chunk_n;
  String data;

  InetAddress sender_addr;
  int sender_port;

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
    PacketInfo stored_packet = new PacketInfo();

    stored_packet.setVersion(this.version);
    stored_packet.setFileID(this.file_id);
    stored_packet.setChunkN(this.chunk_n);
    stored_packet.setData(this.data);
    stored_packet.setAddress(this.sender_addr);
    stored_packet.setPort(this.sender_port);

    mc.sendMsg(stored_packet);
  }
}
