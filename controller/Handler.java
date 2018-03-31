package controller;

import network.*;
import controller.Pair;
import files.FileHandler;
import controller.server.*;
import controller.ApplicationInfo;

import java.net.InetAddress;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Abstract class representing a Handler for a received message or instruction
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public abstract class Handler implements Runnable {
  /**
   * Signals the {@link Handler} that the packet was received
   * @param packet The packet received by the {@link ChannelListener}
   */
  public abstract void signal(PacketInfo packet);

  /**
   * Creates a new {@link Handler} for the given {@link PacketInfo}
   * @param  packet Packet that needs to be processed
   * @return        The new {@link Handler} to process the packet
   */
  public static Handler newHandler(PacketInfo packet) {
    String type = packet.getType();

    if (type.equals("PUTCHUNK")) {
      return new PutchunkHandler(packet, ApplicationInfo.getMC());
    }
    else if (type.equals("GETCHUNK")) {
      return new GetchunkHandler(packet, ApplicationInfo.getMDR());
    }
    else if (type.equals("DELETE")) {
      return new DeleteHandler(packet);
    }
    else if (type.equals("REMOVED")) {
      return new RemovedHandler(packet, ApplicationInfo.getMDB());
    }

    return null;
  }
}
