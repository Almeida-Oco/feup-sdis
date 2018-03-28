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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Handler for the PUTCHUNK message from the network
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class PutchunkHandler extends Handler {
  /**  The ID of the file to store a chunk */
  String file_id;

  /**  The data of the chunk to be stored */
  String data;

  /**  Number of the chunk to be stored */
  int chunk_n;

  /**  Desired replication degree of the chunk */
  int desired_rep;

  /**  Peers who stored the chunk */
  Vector<Integer> replicators;

  /**  Channel to send the STORED message */
  Net_IO mc;

  /**  {@link ScheduledThreadPoolExecutor} to generate a future */
  ScheduledThreadPoolExecutor services;

  /**
   * Initializes the {@link PutchunkHandler}
   * @param packet Packet to be processed
   * @param mc     MC channel
   */
  public PutchunkHandler(PacketInfo packet, Net_IO mc) {
    super();
    this.mc          = mc;
    this.desired_rep = packet.getRDegree();
    this.replicators = new Vector<Integer>(this.desired_rep);
    this.file_id     = packet.getFileID();
    this.chunk_n     = packet.getChunkN();
    this.data        = packet.getData();
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

  @Override
  public void run() {
    PacketInfo      packet = new PacketInfo("STORED", this.file_id, this.chunk_n);
    int             rem_space = File_IO.getRemainingSpace(), data_size = this.data.length();
    Random          rand = new Random();
    ScheduledFuture future;

    packet.setData(this.data);

    // Only stores chunk if the perceived rep degree is smaller than intended
    if (rem_space >= data_size) {
      future = this.services.schedule(()->{
        int actual_rep;
        synchronized (this) {
          actual_rep = this.replicators.size();
        }

        if (actual_rep < this.desired_rep) {
          this.replicators.add(ApplicationInfo.getServID());
          if (File_IO.storeChunk(this.file_id, new FileChunk(this.data.getBytes(StandardCharsets.ISO_8859_1),
          this.data.length(), this.chunk_n, this.desired_rep, this.replicators))) {
            this.mc.sendMsg(packet);
          }
        }
      }, rand.nextInt(401), TimeUnit.MILLISECONDS);

      try {
        future.get();
      }
      catch (Exception err) {
        System.err.println("Putchunk::run() -> Future interrupted!\n - " + err.getMessage());
      }
    }
    else {
      System.out.println("Not enough space to store " + data_size +
          " bytes!\n Space remaining: " + rem_space + " bytes");
    }
  }
}
