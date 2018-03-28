package controller.server;

import files.*;
import controller.Pair;
import controller.Handler;
import network.PacketInfo;

/**
 * Handler for the DELETE message from the network
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class DeleteHandler extends Handler {
  /**
   * The ID of the file to be deleted
   */
  String file_id;

  /**
   * Initializes the {@link DeleteHandler}
   * @param packet Packet to be processed
   */
  public DeleteHandler(PacketInfo packet) {
    super();
    this.file_id = packet.getFileID();
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
    if (File_IO.eraseFileChunks(this.file_id)) {
      System.out.println("Erased chunks of file '" + this.file_id + "'");
    }
  }
}
