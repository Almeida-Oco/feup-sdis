package controller.client;

import files.*;
import network.*;
import controller.Pair;
import controller.Handler;
import controller.ChannelListener;

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
  ChannelListener mc, mdr;

  /** Number of expected chunks to receive */
  int expected_chunks;

  /** The chunks received from the network */
  Set<FileChunk> got_chunks;

  /** The chunks that were not received by the network */
  Set<FileChunk> rem_chunks;

  /**
   * Initializes the {@link RestoreHandler} with the given arguments and executes it
   * @param file_name Path to file to be Restored
   * @param mc        MC {@link ChannelListener}
   * @param mdr       MDR {@link ChannelListener}
   */
  void start(String file_name, ChannelListener mc, ChannelListener mdr) {
    this.file_name  = file_name;
    this.mc         = mc;
    this.mdr        = mdr;
    this.got_chunks = null;
    this.rem_chunks = null;
    this.run();
  }

  @Override
  public void signal(PacketInfo packet) {
    int data_size = packet.getData().length();

    FileChunk chunk = new FileChunk(packet.getData().getBytes(StandardCharsets.ISO_8859_1), data_size, packet.getChunkN(), 0);

    this.got_chunks.add(chunk);
    this.rem_chunks.remove(chunk);
    this.mdr.registerForSignal("CHUNK", packet.getSenderID() + "#" + packet.getChunkN(), this);
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
    FileInfo file = File_IO.getFileInfo(this.file_name);

    if (file == null) {
      System.err.println("File '" + this.file_name + "' does not exist in table!");
      return;
    }

    PacketInfo packet   = new PacketInfo("GETCHUNK", file.getID(), -1);
    int        expected = file.chunkNumber();


    this.got_chunks = Collections.synchronizedSet(new HashSet<FileChunk>(expected, 1));
    this.rem_chunks = Collections.synchronizedSet(new HashSet<FileChunk>(expected, 1));
    for (FileChunk chunk : file.getChunks()) {
      String chunk_id = file.getID() + "#" + chunk.getChunkN();
      packet.setChunkN(chunk.getChunkN());
      this.rem_chunks.add(chunk);

      this.mdr.registerForSignal("CHUNK", chunk_id, this);
      this.mc.sendMsg(packet);
    }

    if (this.waitForRemaining(expected, packet)) {
      File_IO.restoreFile(file.getName(), new Vector<FileChunk>(this.got_chunks));
      System.out.println("Restored file '" + this.file_name + "'!");
    }
    else {
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
    Waiter wait_task = new Waiter(this.got_chunks, expected_chunks, packet, this.mc, this.rem_chunks);

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
  /** Currently received chunks */
  Set<FileChunk> chunks;

  /** Number of expected chunks */
  int expected_chunks;

  /** Packet to use to send messages to network */
  PacketInfo packet;

  /** Channel to send messages */
  ChannelListener mc;

  /** Remaining chunks to send */
  Set<FileChunk> rem_chunks;

  /**
   * Initializes the {@link Waiter}
   * @param chunks   Chunks received
   * @param expected Number of chunks expected to receive
   */
  Waiter(Set<FileChunk> chunks, int expected, PacketInfo packet, ChannelListener mc, Set<FileChunk> rem_chunks) {
    this.chunks          = chunks;
    this.expected_chunks = expected;
    this.packet          = packet;
    this.rem_chunks      = rem_chunks;
    this.mc = mc;
  }

  @Override
  public Boolean call() {
    int size = this.chunks.size();

    for (FileChunk chunk : this.rem_chunks) {
      this.packet.setChunkN(chunk.getChunkN());
      this.mc.sendMsg(this.packet);
    }

    System.out.println("Waiting for chunks (" + size + "/" + this.expected_chunks + ")");
    return size < this.expected_chunks;
  }
}
