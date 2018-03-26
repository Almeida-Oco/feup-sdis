package controller.server;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import controller.ApplicationInfo;

import java.util.Random;
import java.util.Vector;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PutchunkHandler extends Handler {
  byte version;
  String file_id, data;
  int chunk_n, desired_rep;
  Vector<Integer> replicators;
  Net_IO mc;
  ScheduledThreadPoolExecutor services;

  public PutchunkHandler(PacketInfo packet, Net_IO mc) {
    super();
    this.mc          = mc;
    this.desired_rep = packet.getRDegree();
    this.replicators = new Vector<Integer>(this.desired_rep);
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
    synchronized (this) {
      this.replicators.add(packet.getSenderID());
    }
  }

  @Override
  public Pair<String, Handler> register() {
    return new Pair<String, Handler>(this.file_id + "#" + this.chunk_n, this);
  }

  @Override
  public String signalType() {
    return "STORED";
  }

  public void run() {
    PacketInfo packet = new PacketInfo("STORED", this.file_id, this.chunk_n);

    packet.setData(this.data);
    Random rand = new Random();

    // Only stores chunk if the perceived rep degree is smaller than intended
    this.services.schedule(()->{
      int actual_rep;
      synchronized (this) {
        actual_rep = this.replicators.size();
      }

      if (actual_rep < this.desired_rep) {
        File_IO.storeChunk(this.file_id, new FileChunk(this.data.getBytes(StandardCharsets.ISO_8859_1), this.data.length(), this.chunk_n));
        this.mc.sendMsg(packet);
      }
    }, rand.nextInt(401), TimeUnit.MILLISECONDS);
  }
}
