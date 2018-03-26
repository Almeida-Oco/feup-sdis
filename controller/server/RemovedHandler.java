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

public class RemovedHandler extends Handler {
  private static final int MAX_TRIES  = 5;
  private static final long WAIT_TIME = 1000;
  String chunk_id;
  AtomicBoolean got_putchunk;
  PacketInfo packet;
  Net_IO mdb;
  boolean waiting_for_putchunk = true;
  ScheduledThreadPoolExecutor services;

  public RemovedHandler(PacketInfo packet, Net_IO mdb) {
    super();
    this.services     = new ScheduledThreadPoolExecutor(1);
    this.packet       = packet;
    this.got_putchunk = new AtomicBoolean(false);
    this.chunk_id     = packet.getFileID() + "#" + packet.getChunkN();
    this.mdb          = mdb;
  }

  @Override
  public void signal(PacketInfo packet) {
    if (waiting_for_putchunk) {
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

    Listener.registerForSignal("PUTCHUNK", this.chunk_id, this);
    services.schedule(()->{
      if (!this.got_putchunk.get()) {
        waiting_for_putchunk = false;
        Listener.removeFromSignal("PUTCHUNK", this.chunk_id);
        Listener.registerForSignal("STORED", this.chunk_id, this);

        try {
          this.services.schedule(()->this.getConfirmations(1), WAIT_TIME, TimeUnit.MILLISECONDS).get();
        }
        catch (InterruptedException | ExecutionException err) {
          System.err.println("Removed::run() -> Interruped scheduler!\n - " + err.getMessage());
          return;
        }
      }
    }, rand.nextInt(401), TimeUnit.MILLISECONDS);
  }

  private void getConfirmations(int try_n) {
    this.mdb.sendMsg(this.packet);
    if (try_n <= MAX_TRIES && !this.got_putchunk.get()) {
      try {
        this.services.schedule(()->{
          this.getConfirmations(try_n + 1);
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
