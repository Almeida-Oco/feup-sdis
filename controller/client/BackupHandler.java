package controller.client;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import controller.listener.Listener;

import java.rmi.Remote;
import java.util.Vector;
import java.util.Enumeration;
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
  private static final long WAIT_TIME = 1000;

  /** The path of the file to be backed up */
  String file_name;

  /** The desired replication degree of the file */
  int rep_degree;

  /** Instances of the listeners of the MC and MDB channels */
  Listener mc, mdb;

  /** Counter of the 'STORED' messages received */
  ConcurrentHashMap<String, FileChunk> signal_counter;

  /** The {@link ScheduledThreadPoolExecutor} that starts the backup protocol for each chunk */
  ScheduledThreadPoolExecutor services;

  /**
   * Initializes the necessary information for the class and run it
   * @param f_name     Path to file to be replicated
   * @param rep_degree Desired replication degree
   * @param mc         MC {@link Listener} instance
   * @param mdb        MDB {@link Listener} instance
   */
  void start(String f_name, int rep_degree, Listener mc, Listener mdb) {
    this.file_name      = f_name;
    this.rep_degree     = rep_degree;
    this.mc             = mc;
    this.mdb            = mdb;
    this.signal_counter = new ConcurrentHashMap<String, FileChunk>();
    this.services       = new ScheduledThreadPoolExecutor(1);
    this.run();
  }

  @Override
  public void signal(PacketInfo packet) {
    this.signal_counter.get(packet.getFileID() + "#" + packet.getChunkN()).addPeer(packet.getSenderID());
  }

  @Override
  public Pair<String, Handler> register() {
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

      this.signal_counter.put(file.getID() + "#" + chunk.getChunkN(), chunk);
      futures.add(this.sendChunk(packet));
    }

    File_IO.addFile(file);
    for (ScheduledFuture<Void> future : futures) {
      try {
        future.get();
      }
      catch (Exception err) {
        System.err.println("Backup::run() -> Future interrupted!\n - " + err.getMessage());
      }
    }
  }

  /**
   * Sends the given chunk to the network
   * @param  packet The packet to be sent, containing the chunk
   * @return        A {@link ScheduledFuture} that will actually send the chunk
   */
  private ScheduledFuture<Void> sendChunk(PacketInfo packet) {
    String id = packet.getFileID() + "#" + packet.getChunkN();

    this.mc.registerForSignal("STORED", id, this);

    return this.services.schedule(()->{
      return this.getConfirmations(packet, 1, id);
    }, WAIT_TIME, TimeUnit.MILLISECONDS);
  }

  /**
   * Gets the needed confirmations from the network, waiting if needed
   * @param  packet The packet to be sent, containing the chunks
   * @param  try_n  Current try number
   * @param  id     ID of the packet in the {@link SignalCounter}
   * @return        null
   */
  private Void getConfirmations(PacketInfo packet, int try_n, String id) {
    FileChunk chunk             = this.signal_counter.get(id);
    boolean   got_confirmations = chunk.getActualRep() >= chunk.getDesiredRep();

    if (try_n <= MAX_TRIES && !got_confirmations) {
      this.mdb.sendMsg(packet);
      this.services.schedule(()->{
        return this.getConfirmations(packet, try_n + 1, id);
      }, try_n * WAIT_TIME, TimeUnit.MILLISECONDS);
    }
    else if (try_n > MAX_TRIES) {
      System.err.println("Not enough confirmations for packet #" + this.file_name);
      this.mc.removeFromSignal("STORED", id);
      this.services.shutdownNow();
    }
    else if (try_n <= MAX_TRIES && got_confirmations) {
      System.out.println("File '" + this.file_name + "' stored!");
      this.mc.removeFromSignal("STORED", id);
      this.services.shutdownNow();
    }

    return null;
  }
}
