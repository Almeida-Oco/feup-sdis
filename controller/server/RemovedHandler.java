package controller.server;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import controller.SignalHandler;

import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Handler for the REMOVED message from the network
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class RemovedHandler extends Handler {
  private static final int MAX_TRIES  = 5;
  private static final long WAIT_TIME = 1000;

  /** ID of peer that removed file */
  int peer_id;

  /** ID of the file */
  String file_id;

  /** Number of the chunk */
  int chunk_n;

  /** Whether a PUTCHUNK message was received or not */
  AtomicBoolean got_putchunk = new AtomicBoolean(false);

  /** Whether a STORED message was received or not */
  AtomicBoolean got_stored = new AtomicBoolean(false);

  /** If {@link RemovedHandler} is still waiting for putchunk */
  AtomicBoolean waiting_for_putchunk = new AtomicBoolean(true);

  /**  Channel to send the PUTCHUNK message */
  Net_IO mdb;

  /**  {@link ScheduledThreadPoolExecutor} to generate a future */
  ScheduledThreadPoolExecutor services;

  public RemovedHandler(PacketInfo packet, Net_IO mdb) {
    super();
    this.file_id = packet.getFileID();
    this.chunk_n = packet.getChunkN();
    this.peer_id = packet.getSenderID();
    this.mdb     = mdb;
  }

  @Override
  public void signal(PacketInfo packet) {
    if (this.waiting_for_putchunk.get()) {
      this.got_putchunk.set(true);
    }
    else {
      this.got_stored.set(true);
    }
  }

  public void run() {
    Chunk  stored_chunk = FileHandler.getStoredChunk(this.file_id, this.chunk_n);
    Chunk  backed_chunk = FileHandler.getBackedChunk(this.file_id, this.chunk_n);
    Chunk  chunk;
    String chunk_id = this.file_id + "#" + this.chunk_n;

    if (stored_chunk == null&& backed_chunk == null) {
      return;
    }
    else if (stored_chunk == null&& backed_chunk != null) {
      chunk = backed_chunk;
    }
    else {
      chunk = stored_chunk;
    }

    chunk.removePeer(this.peer_id);
    Random rand = new Random();
    ScheduledThreadPoolExecutor services = new ScheduledThreadPoolExecutor(2);
    ScheduledFuture             future;

    SignalHandler.addSignal("PUTCHUNK", chunk_id, this);
    future = services.schedule(()->{
      if (!this.got_putchunk.get()) {
        waiting_for_putchunk.set(false);
        SignalHandler.removeSignal("PUTCHUNK", chunk_id);
        SignalHandler.addSignal("STORED", chunk_id, this);

        try {
          PacketInfo packet = new PacketInfo("PUTCHUNK", this.file_id, this.chunk_n);
          packet.setData(chunk.getData(), chunk.getSize());
          packet.setRDegree(chunk.getDesiredRep());

          this.getConfirmations(services, packet).get();
        }
        catch (InterruptedException | ExecutionException err) {
          System.err.println("Removed::run() -> Interruped inner scheduler!\n - " + err.getMessage());
          return;
        }
      }
    }, rand.nextInt(401), TimeUnit.MILLISECONDS);

    try {
      future.get();
    }
    catch (Exception err) {
      System.err.println("Removed::run() -> Interruped outer scheduler!\n - " + err.getMessage());
    }
    SignalHandler.removeSignal("STORED", chunk_id);
  }

  private Future<Boolean> getConfirmations(ScheduledThreadPoolExecutor services, PacketInfo packet) throws InterruptedException, ExecutionException {
    return services.submit(()->{
      for (int i = 0; i <= MAX_TRIES; i++) {
        this.mdb.sendMsg(packet);
        ScheduledFuture<Boolean> future = services.schedule(()->{
          return this.got_stored.get();
        }, i * WAIT_TIME, TimeUnit.MILLISECONDS);
        if (future.get()) {
          return true;
        }
      }
      return false;
    });
  }
}
