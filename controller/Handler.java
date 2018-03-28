package controller;

import files.*;
import network.*;
import controller.Pair;
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
   * @param packet The packet received by the {@link Listener}
   */
  public abstract void signal(PacketInfo packet);

  /**
   * Registers the {@link Handler} to receive signals when certain messages are received
   * @return {@link Pair} containing a {@link String} representing the fileID and the chunk number of the messages to be signalled,
   *  and a {@link Handler} to be notified, null if {@link Handler} need not be signalled
   */
  public abstract Pair<String, Handler> register();

  /**
   * The type of messages the {@link Handler} wants to be notified of
   * @return {@link String} with type of message, null if {@link Handler} need not be signalled
   */
  public abstract String signalType();

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
    else if (type.equals("STORED")) {
      File_IO.tryIncRep(packet.getFileID(), packet.getChunkN(), packet.getSenderID());
      return null;
    }
    return null;
  }
}
