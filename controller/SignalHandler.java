package controller;

import files.FileHandler;
import network.PacketInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Handles any signalling needed to {@link Handler}
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class SignalHandler implements Runnable {
  private static final int MAX_PACKETS = 2048;


  /** Signals received in need to be processed */
  private static ArrayBlockingQueue<PacketInfo> packets;

  /**
   * Map that holds the {@link Handler} request to be signalled
   * First key is the type of the packet
   * Second key is the file ID and chunk number
   */
  private static ConcurrentHashMap<String, ConcurrentHashMap<String, Handler> > signals;

  static {
    packets = new ArrayBlockingQueue<PacketInfo>(MAX_PACKETS);
    signals = new ConcurrentHashMap<String, ConcurrentHashMap<String, Handler> >(4);
    signals.put("STORED", new ConcurrentHashMap<String, Handler>());
    signals.put("CHUNK", new ConcurrentHashMap<String, Handler>());
    signals.put("PUTCHUNK", new ConcurrentHashMap<String, Handler>());
    signals.put("CHUNKCHKS", new ConcurrentHashMap<String, Handler>());
  }

  /**
   * Adds a new packet to {@link SignalHandler#packets}
   * @param  packet Packet to be processed
   * @return        Whether the packet was stored or not
   */
  static boolean addPacket(PacketInfo packet) {
    try {
      return packets.add(packet);
    }
    catch (IllegalStateException err) {
      System.err.println("Packets signal full!");
      return false;
    }
  }

  /**
   * Registers a new signal request
   * @param signal_type Type of signal to register
   * @param chunk_id    ID of chunk to use (<fileID>#<chunk_number>)
   * @param task        {@link Handler} to signal
   */
  public static void addSignal(String signal_type, String chunk_id, Handler task) {
    ConcurrentHashMap<String, Handler> chunk_task = signals.get(signal_type);
    if (chunk_task == null) {
      chunk_task = new ConcurrentHashMap<String, Handler>();
      signals.put(signal_type, chunk_task);
    }

    chunk_task.put(chunk_id, task);
  }

  /**
   * Removes a signal request
   * @param signal_type Type of signal to remove the request
   * @param chunk_id    ID of chunk to remove the request {<fileID>#<chunk_number>}
   */
  public static void removeSignal(String signal_type, String chunk_id) {
    ConcurrentHashMap<String, Handler> chunk_task = signals.get(signal_type);
    if (chunk_task == null) {
      System.err.println("No signal registered for '" + signal_type + "'");
      return;
    }

    chunk_task.remove(chunk_id);
  }

  @Override
  public void run() {
    PacketInfo packet;

    try {
      do {
        packet = packets.take();
        this.handlePacket(packet);
        this.checkSignal(packet);
      } while (packet != null);
    }
    catch (InterruptedException err) {
      System.err.println("SignalHandler interrupted!\n - " + err.getMessage());
    }
  }

  /**
   * Handles the packet
   * @param packet Packet received
   * Used mostly for processing 'STORED' messages
   */
  private static void handlePacket(PacketInfo packet) {
    String type = packet.getType();

    if (type.equals("STORED")) {
      FileHandler.addNetworkPeer(packet.getFileID(), packet.getChunkN(), 0, packet.getSenderID());
    }
    else if (type.equals("PUTCHUNK")) {
      FileHandler.addNetworkPeer(packet.getFileID(), packet.getChunkN(), packet.getRDegree(), null);
    }
    else if (type.equals("REMOVED")) {
      System.out.println("REm netpeer(" + packet.getFileID() + ", " + packet.getChunkN() + ", " + packet.getSenderID() + ")");
      FileHandler.remNetworkPeer(packet.getFileID(), packet.getChunkN(), packet.getSenderID());
    }
  }

  /**
   * Checks if there is a need to signal the given packet
   * @param packet Packet to check
   */
  private static void checkSignal(PacketInfo packet) {
    ConcurrentHashMap<String, Handler> chunk_tasks = signals.get(packet.getType());
    if (chunk_tasks != null) {
      Handler task = chunk_tasks.get(packet.getFileID() + "#" + packet.getChunkN());

      if (task != null) {
        task.signal(packet);
      }
    }
  }
}
