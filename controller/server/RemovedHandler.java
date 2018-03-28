package controller.server;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import controller.listener.Listener;

import java.util.Random;
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

  /**  ID of the chunk that got removed (<fileID>#<chunk_number>) */
  String chunk_id;

  /** ID of the file */
  String file_id;

  /** Number of the chunk */
  int chunk_n;

  /** The chunk another peer removed */
  FileChunk chunk;

  /** Whether a PUTCHUNK message was received or not */
  AtomicBoolean got_putchunk = new AtomicBoolean(false);

  /** If {@link RemovedHandler} is still waiting for putchunk */
  AtomicBoolean waiting_for_putchunk = new AtomicBoolean(true);

  /**  Channel to send the PUTCHUNK message */
  Net_IO mdb;

  /**  {@link ScheduledThreadPoolExecutor} to generate a future */
  ScheduledThreadPoolExecutor services;

  public RemovedHandler(PacketInfo packet, FileChunk chunk, Net_IO mdb) {
    super();
    this.chunk    = chunk;
    this.services = new ScheduledThreadPoolExecutor(1);
    this.file_id  = packet.getFileID();
    this.chunk_n  = packet.getChunkN();
    this.chunk_id = this.file_id + "#" + this.chunk_n;
    this.mdb      = mdb;
  }

  @Override
  public void signal(PacketInfo packet) {
    if (this.waiting_for_putchunk.get()) {
      this.got_putchunk.set(true);
    }
    else {
    }
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
    Random rand = new Random();
    ScheduledThreadPoolExecutor services = new ScheduledThreadPoolExecutor(1);
    ScheduledFuture             future;

    Listener.registerForSignal("PUTCHUNK", this.chunk_id, this);
    future = services.schedule(()->{
      if (!this.got_putchunk.get()) {
        waiting_for_putchunk.set(false);
        Listener.removeFromSignal("PUTCHUNK", this.chunk_id);
        Listener.registerForSignal("STORED", this.chunk_id, this);

        try {
          PacketInfo packet = new PacketInfo("PUTCHUNK", this.file_id, this.chunk_n);
          packet.setData(chunk.getData(), chunk.getSize());
          packet.setRDegree(chunk.getDesiredRep());

          this.services.schedule(()->this.getConfirmations(packet, 1), WAIT_TIME, TimeUnit.MILLISECONDS).get();
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
  }

  private void getConfirmations(PacketInfo packet, int try_n) {
    this.mdb.sendMsg(packet);
    if (try_n <= MAX_TRIES && !this.got_putchunk.get()) {
      try {
        this.services.schedule(()->{
          this.getConfirmations(packet, try_n + 1);
        }, WAIT_TIME * try_n, TimeUnit.MILLISECONDS).get();
      }
      catch (InterruptedException | ExecutionException err) {
        System.err.println("Removed::getConfirmations -> Interruped scheduler!\n - " + err.getMessage());
        return;
      }
    }
    else {
      this.services.shutdownNow();
    }

    Listener.removeFromSignal("STORED", this.chunk_id);
  }
}
