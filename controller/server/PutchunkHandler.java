package controller.server;

import network.*;
import controller.Pair;
import controller.Handler;
import controller.ApplicationInfo;
import files.*;

import java.util.Random;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PutchunkHandler extends Handler {
  byte version;
  String file_id;
  int chunk_n;
  String data;
  Net_IO mc;
  ScheduledThreadPoolExecutor services;

  public PutchunkHandler(PacketInfo packet, Net_IO mc) {
    super();
    this.mc          = mc;
    this.version     = packet.getVersion();
    this.file_id     = packet.getFileID();
    this.chunk_n     = packet.getChunkN();
    this.data        = packet.getData();
    this.sender_addr = packet.getAddr();
    this.sender_port = packet.getPort();
    this.services    = new ScheduledThreadPoolExecutor(1);
  }

  @Override
  public void signal(PacketInfo packet) {
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
    PacketInfo packet = new PacketInfo(ApplicationInfo.getMC().getAddr(), ApplicationInfo.getMC().getPort());
    Random     rand   = new Random();

    // System.out.println("-- START DATA CHUNK " + this.chunk_n + " --");
    // for (int i = 0; i < 5; i++) {
    //   System.out.println(String.format("  %x", this.data.getBytes(StandardCharsets.ISO_8859_1)[i]));
    // }
    // System.out.println("-- END DATA CHUNK " + this.chunk_n + " --");

    File_IO.storeChunk(this.file_id, new FileChunk(this.data.getBytes(StandardCharsets.ISO_8859_1), this.data.length(), this.chunk_n));
    packet.setType("STORED");
    packet.setFileID(this.file_id);
    packet.setChunkN(this.chunk_n);
    packet.setData(this.data);

    this.services.schedule(()->{
      this.mc.sendMsg(packet);
    }, rand.nextInt(401), TimeUnit.MILLISECONDS);
  }
}
