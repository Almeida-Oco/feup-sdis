package controller.client;

import network.*;
import files.*;
import controller.Handler;
import controller.listener.Listener;
import controller.Pair;

import java.rmi.Remote;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

class RestoreHandler extends Handler implements Remote {
  String file_name;
  Listener mc, mdr;
  int expected_chunks;
  Set<FileChunk> chunks;
  String curr_packet;


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

    this.chunks.add(new FileChunk(packet.getData().getBytes(), data_size, packet.getChunkN()));
  }

  @Override
  public Pair<String, Handler> register() {
    return new Pair<String, Handler>(this.curr_packet, this);
  }

  @Override
  public String signalType() {
    return null;
  }

  @Override
  public void run() {
    FileInfo   file   = File_IO.getFileInfo(this.file_name);
    PacketInfo packet = new PacketInfo(this.mdr.getChannel().getAddr(), this.mdr.getChannel().getPort());

    packet.setType("GETCHUNK");
    packet.setFileID(file.getID());
    this.expected_chunks = file.chunkNumber();

    this.chunks = Collections.synchronizedSet(new HashSet<FileChunk>(this.expected_chunks, 1));

    for (FileChunk chunk : file.getChunks()) {
      this.curr_packet = this.file_name + "#" + chunk.getChunkN();
      packet.setChunkN(chunk.getChunkN());

      this.mdr.getChannel().sendMsg(packet);
      this.mdr.registerForSignal(this);
    }
    if (this.waitForRemaining(file.getChunks())) {
      File_IO.restoreFile(file.getName(), new Vector<FileChunk>(this.chunks));
    }
    else {
      System.err.println("Timed out!\nFailed to recover file " + file.getName());
    }
  }

  private boolean waitForRemaining(Vector<FileChunk> chunks) {
    AtomicInteger            count     = new AtomicInteger(0);
    ScheduledExecutorService schedulor = Executors.newScheduledThreadPool(1);
    ScheduledFuture          future    = schedulor.scheduleAtFixedRate(()->{
      System.out.println("Waiting for chunks (" + this.chunks.size() + "/" + this.expected_chunks + ")");
      count.incrementAndGet();
    }, 0, 2, TimeUnit.SECONDS);

    while (this.chunks.size() < this.expected_chunks && count.get() < 5) {
    }

    future.cancel(false);
    return this.chunks.size() == this.expected_chunks;
  }
}
