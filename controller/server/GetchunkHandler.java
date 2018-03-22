package controller.server;

import network.PacketInfo;
import controller.Pair;
import controller.Handler;
import network.*;
import files.*;

import java.util.Random;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class GetchunkHandler extends Handler {
  boolean got_chunk = false;
  byte version;
  String file_id;
  int chunk_n;
  Net_IO mdr;
  ScheduledThreadPoolExecutor services;

  //TODO should I just store the packet then initialize?
  // How much overhead is added with these initializations?
  public GetchunkHandler(PacketInfo packet, Net_IO mdr) {
    super();
    this.mdr      = mdr;
    this.version  = packet.getVersion();
    this.file_id  = packet.getFileID();
    this.chunk_n  = packet.getChunkN();
    this.services = new ScheduledThreadPoolExecutor(1);
  }

  @Override
  public void signal(PacketInfo packet) {
    synchronized (this) {
      this.got_chunk = true;
    }
  }

  @Override
  public Pair<String, Handler> register() {
    return new Pair<String, Handler>(this.file_id + "#" + this.chunk_n, this);
  }

  @Override
  public String signalType() {
    return "CHUNK";
  }

  public void run() {
    FileChunk chunk = File_IO.getChunk(this.file_id, this.chunk_n);

    if (chunk != null) {
      PacketInfo packet = new PacketInfo(this.sender_addr, this.sender_port);

      packet.setType("CHUNK");
      packet.setVersion(this.version);
      packet.setFileID(this.file_id);
      packet.setChunkN(this.chunk_n);
      packet.setData(chunk.getData(), chunk.getSize());
      Random rand = new Random();

      this.services.schedule(()->{
        synchronized (this) {
          if (!this.got_chunk) {
            this.mdr.sendMsg(packet);
          }
        }
      }, rand.nextInt(401), TimeUnit.MILLISECONDS);
    }
  }
}
