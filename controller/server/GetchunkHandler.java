package controller.server;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import network.PacketInfo;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Handler for the GETCHUNK message from the network
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class GetchunkHandler extends Handler {
  /** Whether a CHUNK message was already sent into the network */
  boolean got_chunk = false;

  /** The ID of the file to get a chunk from */
  String file_id;

  /** The number of the chunk to be sent */
  int chunk_n;

  /** The channel to send the chunk */
  Net_IO mdr;

  /** {@link ScheduledThreadPoolExecutor} to generate a future */
  ScheduledThreadPoolExecutor services;

  /**
   * Initializes the {@link GetchunkHandler}
   * @param packet Packet to be processed
   * @param mdr    MDR channel
   */
  public GetchunkHandler(PacketInfo packet, Net_IO mdr) {
    super();
    this.mdr      = mdr;
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

  @Override
  public void run() {
    FileChunk       chunk = File_IO.getStoredChunk(this.file_id, this.chunk_n);
    ScheduledFuture future;

    if (chunk != null) {
      PacketInfo packet = new PacketInfo("CHUNK", this.file_id, this.chunk_n);
      packet.setData(chunk.getData(), chunk.getSize());

      Random rand = new Random();
      future = this.services.schedule(()->{
        synchronized (this) {
          System.out.println("Got chunk? " + this.got_chunk);
          if (!this.got_chunk) {
            if (this.mdr.sendMsg(packet)) {
              System.out.println("GetChunkHandler::run() -> Msg sent!\n - ID = '" + this.file_id + "#" + this.chunk_n + "'");
            }
            else {
              System.out.println("GetChunkHandler::run() -> Msg not sent!");
            }
          }
        }
      }, rand.nextInt(401), TimeUnit.MILLISECONDS);

      try {
        future.get();
      }
      catch (Exception err) {
        System.err.println("Getchunk::run() -> Future interrupted!\n - " + err.getMessage());
      }
    }
  }
}
