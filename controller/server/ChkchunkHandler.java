package controller.server;

import network.Net_IO;
import controller.Pair;
import files.FileHandler;
import controller.Handler;
import network.PacketInfo;

import java.util.Vector;

/**
 * Handler for the DELETE message from the network
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class ChkchunkHandler extends Handler {
  /** Channel to send response to */
  Net_IO mc;

  /** The ID of the file to be deleted */
  String file_id;

  /** Number of chunk to check */
  int chunk_n;

  /**
   * Initializes the {@link ChkchunkHandler}
   * @param packet Packet to be processed
   */
  public ChkchunkHandler(PacketInfo packet, Net_IO mc) {
    this.file_id = packet.getFileID();
    this.chunk_n = packet.getChunkN();
    this.mc      = mc;
  }

  @Override
  public void signal(PacketInfo packet) {
  }

  @Override
  public void run() {
    Pair<Integer, Vector<Integer> > info = FileHandler.getChunkInfo(this.file_id, this.chunk_n);
    if (info != null) {
      PacketInfo packet = new PacketInfo("CHUNKCHKS", this.file_id, this.chunk_n);
      packet.setRDegree(info.first());
      packet.setReplicators(info.second());
      this.mc.sendMsg(packet);
    }
  }
}
