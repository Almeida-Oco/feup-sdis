package controller.server;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import network.PacketInfo;
import controller.SignalHandler;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Handler for the GETCHUNK message from the network
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class GetchunkHandler extends Handler {
  /** Whether a CHUNK message was already sent into the network */
  AtomicBoolean got_chunk = new AtomicBoolean(false);

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
    System.out.print("Got chunk #" + packet.getChunkN() + "!\n");
    this.got_chunk.set(true);
  }

  @Override
  public void run() {
    Chunk           chunk = FileHandler.getStoredChunk(this.file_id, this.chunk_n);
    ScheduledFuture future;
    String          chunk_id = this.file_id + "#" + this.chunk_n;

    if (chunk != null) {
      SignalHandler.addSignal("CHUNK", chunk_id, this);
      PacketInfo packet = new PacketInfo("CHUNK", this.file_id, this.chunk_n);
      packet.setData(chunk.getData(), chunk.getSize());

      Random rand = new Random();
      future = this.services.schedule(()->{
        if (!this.got_chunk.get()) {
          this.mdr.sendMsg(packet);
        }
      }, rand.nextInt(401), TimeUnit.MILLISECONDS);

      try {
        future.get();
      }
      catch (Exception err) {
        System.err.println("Getchunk::run() -> Future interrupted!\n - " + err.getMessage());
      }
      SignalHandler.removeSignal("CHUNK", chunk_id);
    }
  }
}
