package controller.client;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import controller.SignalCounter;
import controller.listener.Listener;

import java.rmi.Remote;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

class BackupHandler extends Handler implements Remote {
  private static final int MAX_TRIES  = 5;
  private static final long WAIT_TIME = 1000;
  String file_name;
  int rep_degree;
  Listener mc, mdb;
  SignalCounter signals;
  String curr_packet = null;
  ScheduledThreadPoolExecutor services;

  void start(String f_name, int rep_degree, Listener mc, Listener mdb) {
    this.file_name  = f_name;
    this.rep_degree = rep_degree;
    this.mc         = mc;
    this.mdb        = mdb;
    this.signals    = new SignalCounter(rep_degree);
    this.services   = new ScheduledThreadPoolExecutor(1);
    this.run();
  }

  //TODO missing saving the peer that responded
  @Override
  public void signal(PacketInfo packet) {
    this.signals.signalValue(packet.getFileID() + "#" + packet.getChunkN());
  }

  @Override
  public Pair<String, Handler> register() {
    if (this.curr_packet != null) {
      return new Pair<String, Handler>(this.curr_packet, this);
    }
    return null;
  }

  @Override
  public String signalType() {
    return "STORED";
  }

  @Override
  public void run() {
    FileInfo file = File_IO.readFile(this.file_name, this.rep_degree);

    Vector<FileChunk> chunks = file.getChunks();
    Vector<ScheduledFuture<Void> > futures = new Vector<ScheduledFuture<Void> >(chunks.size());

    for (FileChunk chunk : chunks) {
      PacketInfo packet = new PacketInfo("PUTCHUNK", file.getID(), chunk.getChunkN());
      packet.setRDegree(this.rep_degree);
      packet.setData(chunk.getData(), chunk.getSize());

      this.signals.registerValue(file.getID(), chunk.getChunkN(), packet.getRDegree());
      futures.add(this.sendChunk(packet));
    }

    File_IO.addFile(file);
    this.curr_packet = null;
    for (ScheduledFuture<Void> future : futures) {
      try {
        future.get();
      }
      catch (Exception err) {
        System.err.println("Backup::run() -> Future interrupted!\n - " + err.getMessage());
      }
    }
  }

  private ScheduledFuture<Void> sendChunk(PacketInfo packet) {
    int    wait_time = 1000, tries = 0;
    String id = packet.getFileID() + "#" + packet.getChunkN();

    this.curr_packet = id;
    this.mc.registerForSignal(id, "STORED", this);

    return this.services.schedule(()->{
      return this.getConfirmations(packet, tries + 1, id);
    }, wait_time, TimeUnit.MILLISECONDS);
  }

  private Void getConfirmations(PacketInfo packet, int try_n, String id) {
    this.mdb.sendMsg(packet);
    boolean got_confirmations = this.signals.confirmations(id) >= this.signals.maxNumber();

    if (try_n <= MAX_TRIES && !got_confirmations) {
      this.services.schedule(()->{
        return this.getConfirmations(packet, try_n + 1, id);
      }, try_n * WAIT_TIME, TimeUnit.MILLISECONDS);
    }
    else if (try_n > MAX_TRIES) {
      System.err.println("Not enough confirmations for packet #" + this.file_name);
      this.mc.removeFromSignal(this);
    }
    else if (try_n <= MAX_TRIES && got_confirmations) {
      System.out.println("File '" + this.file_name + "' stored!");
      this.mc.removeFromSignal(this);
      this.services.shutdownNow();
    }
    return null;
  }
}
