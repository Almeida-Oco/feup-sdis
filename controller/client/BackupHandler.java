package controller.client;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import controller.listener.Listener;

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
    this.run();
  }

  @Override
  public void signal(PacketInfo packet) {
    System.out.println("Signalled Chunk #" + packet.getChunkN());
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
    boolean all_good = true;

    Vector<FileChunk> chunks = file.getChunks();
    this.services       = new ScheduledThreadPoolExecutor(chunks.size() * 2);
    Vector<Future<Boolean> > futures = new Vector<Future<Boolean> >(chunks.size());

    for (FileChunk chunk : chunks) {
      PacketInfo packet = new PacketInfo("PUTCHUNK", file.getID(), chunk.getChunkN());
      packet.setRDegree(this.rep_degree);
      packet.setData(chunk.getData(), chunk.getSize());

      this.signal_counter.put(file.getID() + "#" + chunk.getChunkN(), chunk);
      futures.add(this.sendChunk(packet));
    }

    try {
      for (Future<Boolean> future : futures) {
          all_good = all_good && future.get();
        }
    }
    catch (Exception err) {
        System.err.println("Backup::run() -> Future interrupted!\n - " + err.getMessage());
        all_good = false;
    }

    if (all_good) {
      System.out.println("File '" + this.file_name + "' stored!");
    }
    else {
      System.out.println("File '" + this.file_name + "' stored with less than desired replication degree!");
    }
    this.services.shutdownNow();
    File_IO.addFile(file);
  }

  /**
   * Sends the given chunk to the network
   * @param  packet The packet to be sent, containing the chunk
   * @return        A {@link Future} that will actually send the chunk
   */
  private Future<Boolean> sendChunk(PacketInfo packet) {
    String id = packet.getFileID() + "#" + packet.getChunkN();
    this.mc.registerForSignal("STORED", id, this);

    return this.getConfirmations(packet, 0, id);
  }

  /**
   * Gets the needed confirmations from the network, waiting if needed
   * @param  packet The packet to be sent, containing the chunks
   * @param  try_n  Current try number
   * @param  id     ID of the packet in the {@link SignalCounter}
   * @return        null
   */
  private Future<Boolean> getConfirmations(PacketInfo packet, int try_n, String id) {
    FileChunk chunk             = this.signal_counter.get(id);
    boolean   got_confirmations = chunk.getActualRep() >= chunk.getDesiredRep();

    return this.services.submit(() -> {
      for (int i = 0; i <= MAX_TRIES; i++) {
        this.mdb.sendMsg(packet);
        ScheduledFuture<Boolean> future = this.services.schedule(() -> {
          return chunk.getActualRep() >= chunk.getDesiredRep();
        }, i * WAIT_TIME, TimeUnit.MILLISECONDS);

        try {
          boolean over = future.get();
          if (over) {
            this.mc.removeFromSignal("STORED", id);
            return true;
          }
        }
        catch (Exception err) {
          System.err.println("Backup::getConfirmations() -> Interrupted future!\n - " + err.getMessage());
        }
      }
      this.mc.removeFromSignal("STORED", id);
      return false;
    });
  }
}
