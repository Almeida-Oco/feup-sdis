package controller.client;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import controller.listener.Listener;

import java.util.Set;
import java.rmi.Remote;
import java.util.Vector;
import java.util.HashSet;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Handler for the Restore instruction from the client
 * @author Gonçalo Moreno
 * @author João Almeida
 */
class RestoreHandler extends Handler implements Remote {
  /** Path to file to be restored */
  String file_name;
  Listener mc, mdr;

  /** Number of expected chunks to receive */
  int expected_chunks;

  /** The chunks received from the network */
  Set<FileChunk> chunks;

  /**
   * Initializes the {@link RestoreHandler} with the given arguments and executes it
   * @param file_name Path to file to be Restored
   * @param mc        MC {@link Listener}
   * @param mdr       MDR {@link Listener}
   */
  void start(String file_name, Listener mc, Listener mdr) {
    this.file_name = file_name;
    this.mc        = mc;
    this.mdr       = mdr;
    this.chunks    = null;
    this.run();
  }

  @Override
  public void signal(PacketInfo packet) {
    int data_size = packet.getData().length();

    this.chunks.add(new FileChunk(packet.getData().getBytes(StandardCharsets.ISO_8859_1), data_size, packet.getChunkN(), 0));
  }

  @Override
  public Pair<String, Handler> register() {
    return null;
  }

  @Override
  public String signalType() {
    return null;
  }

  @Override
  public void run() {
    FileInfo   file   = File_IO.getFileInfo(this.file_name);
    PacketInfo packet = new PacketInfo("GETCHUNK", file.getID(), -1);

    if (file == null) {
      System.err.println("File '" + this.file_name + "' does not exist in table!");
      return;
    }
    int expected = file.chunkNumber();


    this.chunks = Collections.synchronizedSet(new HashSet<FileChunk>(expected, 1));
    for (FileChunk chunk : file.getChunks()) {
      String chunk_id = file.getID() + "#" + chunk.getChunkN();
      packet.setChunkN(chunk.getChunkN());

      this.mdr.registerForSignal("CHUNK", chunk_id, this);
      this.mc.sendMsg(packet);
    }

    if (this.waitForRemaining(file.getChunks(), expected)) {
      File_IO.restoreFile(file.getName(), new Vector<FileChunk>(this.chunks));
      System.out.println("Restored file '" + this.file_name + "'!");
    }
    else {
      System.err.println("Timed out!\nFailed to recover file " + this.file_name);
    }
  }

  /**
   * Waits for the remaining chunks for at most 10 seconds
   * @param  chunks          Currently received chunks
   * @param  expected_chunks Number of chunks expected
   * @return                 Whether all the chunks where received or not
   */
  private boolean waitForRemaining(Vector<FileChunk> chunks, int expected_chunks) {
    int i = 0;

    ScheduledFuture<Boolean>    future;
    ScheduledThreadPoolExecutor schedulor = new ScheduledThreadPoolExecutor(1);
    Waiter wait_task = new Waiter(this.chunks, expected_chunks);

    for (i = 0; i < 5; i++) {
      future = schedulor.schedule(wait_task, 2, TimeUnit.SECONDS);
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
  /**
   * Currently received chunks
   */
  Set<FileChunk> chunks;

  /**
   * Number of expected chunks
   */
  int expected_chunks;

  /**
   * Initializes the {@link Waiter}
   * @param chunks   Chunks received
   * @param expected Number of chunks expected to receive
   */
  Waiter(Set<FileChunk> chunks, int expected) {
    this.chunks          = chunks;
    this.expected_chunks = expected;
  }

  @Override
  public Boolean call() {
    int size = this.chunks.size();

    System.out.println("Waiting for chunks (" + size + "/" + this.expected_chunks + ")");
    return size < this.expected_chunks;
  }
}
