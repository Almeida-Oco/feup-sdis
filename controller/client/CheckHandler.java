package controller.client;

import network.Net_IO;
import controller.Pair;
import files.FileHandler;
import controller.Handler;
import network.PacketInfo;
import controller.SignalHandler;


import java.rmi.Remote;
import java.util.Arrays;
import java.util.Vector;
import java.util.HashSet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Handler for the Check instruction from the client
 * @author Gonçalo Moreno
 * @author João Almeida
 * This handler is only used at the beginning of the execution, to see which chunks the peer should mantain and which should it store
 */
class CheckHandler extends Handler implements Remote {
  private static final int MAX_TRIES = 3;

  /** Channel to send CHKCHUNK */
  Net_IO mc;

  /** The chunks to be checked */
  Vector<Pair<String, Integer> > chunks;

  /** The chunks that were checked by the network, this hashmap is initialized with all false */
  ConcurrentHashMap<String, Pair<Integer, HashSet<Integer> > > checked_chunks;

  ScheduledThreadPoolExecutor services;


  void start(Vector<Pair<String, Integer> > chunks, Net_IO mc) {
    this.chunks         = chunks;
    this.mc             = mc;
    this.services       = new ScheduledThreadPoolExecutor(chunks.size() * 2);
    this.checked_chunks = new ConcurrentHashMap<String, Pair<Integer, HashSet<Integer> > >(chunks.size(), 1);
    for (Pair<String, Integer> pair : chunks) {
      String chunk_id = pair.first() + "#" + pair.second();
      this.checked_chunks.put(chunk_id, null);
      SignalHandler.addSignal("CHUNKCHKS", chunk_id, this);
    }
    this.run();
  }

  @Override
  public void signal(PacketInfo packet) {
    String chunk_id   = packet.getFileID() + "#" + packet.getChunkN();
    int    rep_degree = packet.getRDegree();

    Pair<Integer, HashSet<Integer> > reps = this.checked_chunks.get(chunk_id);

    if (reps == null) {
      Pair<Integer, HashSet<Integer> > info = new Pair<Integer, HashSet<Integer> >(packet.getRDegree(), this.createHashSet(packet.getReplicators()));
      this.checked_chunks.put(chunk_id, info);
    }
    else {                               //IF already present update information
      synchronized (reps) {
        if (reps.first() < rep_degree) { //Always store highest replcation degree
          reps.setFirst(rep_degree);
        }
        for (int rep : packet.getReplicators()) {
          reps.second().add(rep);
        }
      }
    }
  }

  private HashSet<Integer> createHashSet(int[] replicators) {
    HashSet<Integer> reps = new HashSet<Integer>(replicators.length);
    for (int rep : replicators) {
      reps.add(rep);
    }

    return reps;
  }

  @Override
  public void run() {
    Vector<Future<Void> > futures = new Vector<Future<Void> >(this.chunks.size());
    this.chunks.forEach((pair)->{
      String file_id    = pair.first();
      int chunk_n       = pair.second();
      PacketInfo packet = new PacketInfo("CHKCHUNK", file_id, chunk_n);
      futures.add(this.queryChunk(packet, file_id + "#" + chunk_n));
    });

    for (Future<Void> future : futures) {
      try {
        future.get();
      }
      catch (Exception err) {
      }
    }

    this.checked_chunks.forEach(1, (chunk_id, info)->{
      if (info != null) {
        FileHandler.reuseChunk(chunk_id, info.first(), info.second());
      }
      else {
        FileHandler.remFilesystemChunk(chunk_id);
      }
      SignalHandler.removeSignal("CHUNKCHKS", chunk_id);
    });
  }

  private Future<Void> queryChunk(PacketInfo packet, String chunk_id) {
    return this.services.submit(()->{
      for (int i = 0; i < MAX_TRIES; i++) {
        this.mc.sendMsg(packet);
        ScheduledFuture<Boolean> future = this.services.schedule(()->{
          Pair<Integer, HashSet<Integer> > reps = this.checked_chunks.get(chunk_id);
          return reps != null;
        }, 1, TimeUnit.SECONDS);

        try {
          if (future.get()) {
            return null;
          }
        }
        catch (Exception err) {
          return null;
        }
      }
      return null;
    });
  }
}
