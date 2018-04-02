package controller.client;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import controller.SignalHandler;

import java.rmi.Remote;
import java.util.Vector;
import java.util.Enumeration;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Handler for the Backup instruction from the client
 * @author Gonçalo Moreno
 * @author João Almeida
 */
class BackupHandler extends Handler implements Remote {
  /** Maximum number of tries when backup fails */
  private static final int MAX_TRIES = 5;

  /** Base wait time the protocol will wait for 'STORED' messages */
  private static final long WAIT_TIME = 1;

  /** The path of the file to be backed up */
  String file_name;

  /** The desired replication degree of the file */
  int rep_degree;

  /** Instances of the listeners of the MC and MDB channels */
  Net_IO mc, mdb;

  /** The {@link ScheduledThreadPoolExecutor} that starts the backup protocol for each chunk */
  ScheduledThreadPoolExecutor services;

  /**
   * Initializes the necessary information for the class and run it
   * @param f_name     Path to file to be replicated
   * @param rep_degree Desired replication degree
   * @param mc         MC {@link Net_IO} instance
   * @param mdb        MDB {@link Net_IO} instance
   */
  void start(String f_name, int rep_degree, Net_IO mc, Net_IO mdb) {
    this.file_name  = f_name;
    this.rep_degree = rep_degree;
    this.mc         = mc;
    this.mdb        = mdb;
    this.services   = new ScheduledThreadPoolExecutor(2);
    this.run();
  }

  @Override
  public void signal(PacketInfo packet) {
  }

  @Override
  public void run() {
    FileInfo file = FileHandler.readFile(this.file_name, this.rep_degree);

    if (file == null) {
      return;
    }
    Vector<Chunk>      chunks  = file.getChunks();
    Vector<PacketInfo> packets = new Vector<PacketInfo>(chunks.size());

    for (Chunk chunk : chunks) {
      PacketInfo packet   = new PacketInfo("PUTCHUNK", file.getID(), chunk.getChunkN());
      String     chunk_id = file.getID() + "#" + chunk.getChunkN();
      packet.setRDegree(this.rep_degree);
      packet.setData(chunk.getData(), chunk.getSize());
      packets.add(chunk.getChunkN(), packet);
      this.mdb.sendMsg(packet);
    }

    if (this.checkConfirmations(packets, file)) {
      System.out.println("File '" + this.file_name + "' stored!");
    }
    else {
      System.out.println("File '" + this.file_name + "' stored with less than desired replication degree!");
    }
    this.services.shutdown();
  }

  /**
   * Checks if there are still chunks underly replicated and if so resends the PUTCHUNK message
   * @param  packets Packets to be send in case chunk is underly replicated
   * @param  info    Information about the file backed up
   * @return         Whether all chunks are with at least the desired replication degree or not
   * packets contains all packets previously sent, to avoid creating unnecessary new packets, since that has a huge overhead
   * info is used to get the chunks which are still underly replicated
   */
  private boolean checkConfirmations(Vector<PacketInfo> packets, FileInfo info) {
    ScheduledFuture<Boolean> major_future = this.services.schedule(()->{
      for (int i = 1; i < MAX_TRIES; i++) {
        Vector<Chunk> chunks = info.underlyReplicated();
        for (Chunk chunk : chunks) {
          System.out.println("  Sending #" + chunk.getChunkN());
          this.mdb.sendMsg(packets.get(chunk.getChunkN()));
        }

        ScheduledFuture<Boolean> minor_future = this.services.schedule(()->{
          return info.underlyReplicated().size() == 0;
        }, WAIT_TIME * (i + 1), TimeUnit.SECONDS);

        try {
          if (minor_future.get()) {
            return true;
          }
        }
        catch (Exception err) {
        }
      }
      return false;
    }, WAIT_TIME, TimeUnit.SECONDS);

    try {
      return major_future.get();
    }
    catch (Exception err) {
      return info.underlyReplicated().size() == 0;
    }
  }
}
