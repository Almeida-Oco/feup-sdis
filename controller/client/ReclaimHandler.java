package controller.client;

import network.*;
import files.Chunk;
import controller.Pair;
import files.FileHandler;
import controller.Handler;

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
  Net_IO mc;

  /**
   * Stores the information needed for the protocol and then executes it
   * @param space Number of bytes to be reclaimed
   * @param mc    MC {@link Net_IO}
   */
  void start(int space, Net_IO mc) {
    this.space = space;
    this.mc    = mc;
    this.run();
  }

  @Override
  public void signal(PacketInfo packet) {
  }

  @Override
  public void run() {
    int max_space = FileHandler.getMaxSpace(), used_space = FileHandler.getUsedSpace();

    System.out.println("Max = " + max_space + ", used = " + used_space + ", got = " + this.space);

    if (this.space < 0) {
      System.out.println("Space smaller than 0!");
      return;
    }
    FileHandler.setMaxSpace(this.space);

    if (used_space > this.space) {   //Using more space than it should
      Vector<Pair<String, Chunk> > chunks = FileHandler.reclaimSpace(this.space);
      PacketInfo packet = new PacketInfo("REMOVED", null, 0);

      for (Pair<String, Chunk> pair : chunks) {
        String file_id = pair.first();
        int    chunk_n = pair.second().getChunkN();
        packet.setFileID(file_id);
        packet.setChunkN(chunk_n);

        this.mc.sendMsg(packet);
        System.out.println("Removing " + file_id + "#" + chunk_n);
        FileHandler.remLocalChunk(file_id, chunk_n);
      }
    }
  }
}
