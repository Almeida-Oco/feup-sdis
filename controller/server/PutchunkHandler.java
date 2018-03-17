package controller.server;

import network.Net_IO;
import network.PacketInfo;
import files.*;
import java.net.InetAddress;
import java.util.Random;

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

  @Override
  public void signal(String file_id, int chunk_n) {
  }

  @Override
  public Pair<String, Handler> register() {
    return null;
  }

  @Override
  public String signalType() {
    return null;
  }

  public void run() {
    File_IO.storeChunk(this.file_id, new FileChunk(this.data.getBytes(), this.data.length(), this.chunk_n));
    PacketInfo packet = new PacketInfo(this.sender_addr, this.sender_port);

    packet.setType("STORED");
    packet.setVersion(this.version);
    packet.setFileID(this.file_id);
    packet.setChunkN(this.chunk_n);
    packet.setData(this.data);
    Random rand = new Random();
    try {
      Thread.sleep(rand.nextInt(401)); //TODO use ScheduledExecutorService?
    }
    catch (InterruptedException err) {
      System.out.println("PutchunkHandler failed to sleep!\n - " + err.getMessage());
    }
    this.mc.sendMsg(packet);
  }
}
