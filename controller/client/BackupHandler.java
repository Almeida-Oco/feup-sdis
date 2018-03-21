package controller.client;

import network.*;
import files.*;
import controller.SignalCounter;
import controller.Handler;
import controller.listener.Listener;
import controller.Pair;

import java.rmi.Remote;

class BackupHandler extends Handler implements Remote {
  String file_name;
  int rep_degree;
  Listener mc, mdb;
  SignalCounter signals;
  String curr_packet = null;

  void start(String f_name, int rep_degree, Listener mc, Listener mdb) {
    this.file_name  = f_name;
    this.rep_degree = rep_degree;
    this.mc         = mc;
    this.mdb        = mdb;
    this.signals    = new SignalCounter(rep_degree);
    this.run();
  }

  //TODO missing saving the peer that responded
  @Override
  public void signal(PacketInfo packet) {
    System.err.println("Got signal of packet " + packet.getType());
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
    FileInfo   file   = File_IO.readFile(this.file_name, this.rep_degree);
    PacketInfo packet = new PacketInfo(this.mdb.getChannel().getAddr(), this.mdb.getChannel().getPort());

    packet.setRDegree(this.rep_degree);
    packet.setType("PUTCHUNK");
    packet.setFileID(file.getID());

    for (FileChunk chunk : file.getChunks()) {
      packet.setChunkN(chunk.getChunkN());
      packet.setData(chunk.getData(), chunk.getSize());

      this.signals.registerValue(file.getID(), chunk.getChunkN());
      System.out.println("Sending chunk #" + packet.getChunkN());
      if (!this.sendChunk(packet)) {
        System.err.println("Not enough confirmations for packet #" + chunk.getChunkN());
      }
    }

    File_IO.addFile(file);
    this.curr_packet = null;
    System.out.println("BACKUP");
  }

  private boolean sendChunk(PacketInfo packet) {
    int    wait_time = 1000, tries = 1;
    String id = packet.getFileID() + "#" + packet.getChunkN();

    this.curr_packet = id;
    this.mc.registerForSignal(this);

    while (tries <= 5 && this.signals.confirmations(id) < this.signals.maxNumber()) {
      this.mdb.getChannel().sendMsg(packet);

      try {
        Thread.sleep(wait_time * tries);
        System.out.println("Confirmations = " + this.signals.confirmations(id));
      }
      catch (InterruptedException err) {
        System.err.println("Failed to sleep for " + (wait_time * tries) + "ms");
      }

      tries++;
    }

    this.mc.removeFromSignal(this);
    return tries <= 5 && this.signals.confirmations(id) >= this.signals.maxNumber();
  }
}
