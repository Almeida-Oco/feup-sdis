package controller.client;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import controller.SignalHandler;

import java.util.Set;
import java.rmi.Remote;
import java.util.Vector;
import java.util.HashSet;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ExecutionException;
import java.nio.channels.AsynchronousFileChannel;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Handler for the Restore instruction from the client
 * @author Gonçalo Moreno
 * @author João Almeida
 */
class RestoreHandler extends Handler implements Remote {
  private static final int MAX_CHUNK_SIZE = 64000;
  private static final int MAX_RESENDS    = 5;
  private static final int WAIT_TIME      = 2;

  /** Path to file to be restored */
  String file_name;

  /** Instances of MC and MDR channels */
  Net_IO mc, mdr;

  /** The chunks that were not received by the network */
  Set<Integer> rem_chunks;

  /** Futures returned by AsynchronousFileChannel write */
  Vector<Future<Integer> > futures;

  /** Channel to write data to */
  AsynchronousFileChannel out;

  /**
   * Initializes the {@link RestoreHandler} with the given arguments and executes it
   * @param file_name Path to file to be Restored
   * @param mc        MC {@link Net_IO}
   * @param mdr       MDR {@link Net_IO}
   */
  void start(String file_name, Net_IO mc, Net_IO mdr) {
    this.file_name  = file_name;
    this.mc         = mc;
    this.mdr        = mdr;
    this.rem_chunks = null;
    this.out        = null;
    this.futures    = null;
    this.run();
  }

  @Override
  public void signal(PacketInfo packet) {
    int chunk_n = packet.getChunkN();

    if (this.rem_chunks.contains(chunk_n)) {
      byte[] data = packet.getData().getBytes(StandardCharsets.ISO_8859_1);
      this.rem_chunks.remove(Integer.valueOf(chunk_n));
      SignalHandler.removeSignal("CHUNK", packet.getSenderID() + "#" + packet.getChunkN());

      this.futures.add(this.out.write(ByteBuffer.wrap(data), MAX_CHUNK_SIZE * chunk_n));
    }
  }

  @Override
  public void run() {
    FileInfo file = FileHandler.getBackedFileByName(this.file_name);

    if (file == null) {
      System.err.println("File '" + this.file_name + "' does not exist in table!");
      return;
    }
    if ((this.out = FileHandler.openAsyncWriter(file_name)) == null) {
      return;
    }

    PacketInfo packet   = new PacketInfo("GETCHUNK", file.getID(), -1);
    int        expected = file.chunkNumber();
    this.futures = new Vector<Future<Integer> >(expected);

    this.rem_chunks = Collections.synchronizedSet(new HashSet<Integer>(expected, 1));
    for (Chunk chunk : file.getChunks()) {
      String chunk_id = file.getID() + "#" + chunk.getChunkN();
      packet.setChunkN(chunk.getChunkN());
      this.rem_chunks.add(chunk.getChunkN());

      SignalHandler.addSignal("CHUNK", chunk_id, this);
      this.mc.sendMsg(packet);
    }

    if (this.waitForRemaining(expected, packet)) {
      this.futures.forEach((future)->{
        try {
          future.get();
        }
        catch (ExecutionException | InterruptedException err) {
          System.err.println("Interruped restore future!");
        }
      });
      FileHandler.closeAsyncWriter(this.out);
      System.out.println("Restored file '" + this.file_name + "'!");
    }
    else {
      FileHandler.eraseBackedFile(this.file_name);
      System.err.println("Timed out!\nFailed to recover file " + this.file_name);
    }
  }

  /**
   * Waits for the remaining chunks for at most 10 seconds
   * @param  expected_chunks Number of chunks expected
   * @return                 Whether all the chunks where received or not
   */
  private boolean waitForRemaining(int expected_chunks, PacketInfo packet) {
    int i = 0;

    ScheduledFuture<Boolean>    future;
    ScheduledThreadPoolExecutor schedulor = new ScheduledThreadPoolExecutor(1);
    Waiter wait_task = new Waiter(expected_chunks, packet, this.mc, this.rem_chunks);

    for (i = 0; i < MAX_RESENDS; i++) {
      future = schedulor.schedule(wait_task, WAIT_TIME, TimeUnit.SECONDS);
      try {
        if (!future.get()) { //Meaning it has received all chunks
          break;
        }
      }
      catch (Exception err) {
        System.err.println("RestoreHandler::run() -> Error waiting for thread!\n - " + err.getMessage());
      }
    }

    return i < 5;
  }
}

/**
 * {@link Callable} that waits for all chunks to be received
 * @author Gonçalo Moreno
 * @author João Almeida
 */
class Waiter implements Callable<Boolean> {
  /** Number of expected chunks */
  int expected_chunks;

  /** Packet to use to send messages to network */
  PacketInfo packet;

  /** Channel to send messages */
  Net_IO mc;

  /** Remaining chunks to send */
  Set<Integer> rem_chunks;

  /**
   * Initializes the {@link Waiter}
   * @param chunks   Chunks received
   * @param expected Number of chunks expected to receive
   */
  Waiter(int expected, PacketInfo packet, Net_IO mc, Set<Integer> rem_chunks) {
    this.expected_chunks = expected;
    this.packet          = packet;
    this.rem_chunks      = rem_chunks;
    this.mc = mc;
  }

  @Override
  public Boolean call() {
    int size = this.rem_chunks.size();

    for (Integer chunk_n : this.rem_chunks) {
      this.packet.setChunkN(chunk_n);
      this.mc.sendMsg(this.packet);
    }

    System.out.println("Waiting for chunks (" + (this.expected_chunks - size) + "/" + this.expected_chunks + ")");
    return size > 0;
  }
}
