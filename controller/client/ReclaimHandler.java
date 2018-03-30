package controller.client;

import network.*;
import files.File_IO;
import controller.Pair;
import files.FileChunk;
import controller.Handler;
import controller.ChannelListener;

import java.rmi.Remote;
import java.util.Vector;

/**
 * Handler for the Reclaim instruction from the client
 * @author Gonçalo Moreno
 * @author João Almeida
 */
class ReclaimHandler extends Handler implements Remote {
  /** Bytes in disk to be reclaimed */
  int space;
  ChannelListener mc;

  /**
   * Stores the information needed for the protocol and then executes it
   * @param space Number of bytes to be reclaimed
   * @param mc    MC {@link ChannelListener}
   */
  void start(int space, ChannelListener mc) {
    this.space = space;
    this.mc    = mc;
    this.run();
  }

  @Override
  public void signal(PacketInfo packet) {
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
    int max_space = File_IO.getMaxSpace(), used_space = File_IO.getUsedSpace();

    if (this.space < 0) {
      System.out.println("Space smaller than 0!");
      return;
    }

    if (max_space < this.space) {
      File_IO.setMaxSpace(this.space);
    }
    else if (max_space > this.space) {
      File_IO.setMaxSpace(this.space);

      if (used_space > this.space) { //Using more space than it should
        Vector<Pair<String, FileChunk> > chunks = File_IO.reclaimSpace(this.space);
        PacketInfo packet = new PacketInfo("REMOVED", null, 0);

        for (Pair<String, FileChunk> pair : chunks) {
          String file_id = pair.first();
          int    chunk_n = pair.second().getChunkN();
          packet.setFileID(file_id);
          packet.setChunkN(chunk_n);

          this.mc.sendMsg(packet);
          System.out.println("Removing " + file_id + "#" + chunk_n);
          File_IO.eraseChunk(file_id, chunk_n, true);
        }
      }
    }
  }
}
