package controller;

import network.Net_IO;
import controller.Pair;
import network.PacketInfo;
import controller.Handler;
import controller.ApplicationInfo;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Listens for messages on a given Multicast channel
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class ChannelListener implements Runnable {
  /** The channel to listen to */
  Net_IO channel;

  /** Queue to store to be processed received messages */
  ThreadPoolExecutor task_queue;

  /**
   * Map that holds the {@link Handler} request to be signalled
   * First key is the type of the packet
   * Second key is the file ID and chunk number
   */
  private static ConcurrentHashMap<String, ConcurrentHashMap<String, Handler> > signals = new ConcurrentHashMap<String, ConcurrentHashMap<String, Handler> >(4);

  /**
   * Initializes the {@link ChannelListener}
   * @param channel Channel to listen to
   * @param tasks   Queue to store to be processed messages
   */
  public ChannelListener(Net_IO channel, ThreadPoolExecutor tasks) {
    this.channel    = channel;
    this.task_queue = tasks;
  }

  @Override
  public void run() {
    String     txt = null;
    PacketInfo packet;

    do {
      if ((packet = this.channel.recvMsg()) != null&& (packet.getSenderID() != ApplicationInfo.getServID())) {
        this.handleTask(Handler.newHandler(packet), packet);
      }
    } while (txt == null);

    this.task_queue.shutdown();
  }

  /**
   * Processes the {@link Handler} generated to process the {@link PacketInfo}
   * @param task   {@link Handler} generated
   * @param packet Message received
   * If the task is not null, then the packet needs to be processed, so it is sent to {@link ChannelListener#task_queue}
   * IF the task is null, then there might be some {@link Handler} which wishes to be notified that the packet has arrived, so we check for that case
   */
  private void handleTask(Handler task, PacketInfo packet) {
    if (task != null) {
      this.registerForSignal(task.register());
      this.task_queue.execute((Runnable)task);
    }
    else {
      this.signal(packet);
    }
  }

  /**
   * Checks if a {@link Handler} needs to be signalled about the message reception
   * @param packet Message received
   */
  private void signal(PacketInfo packet) {
    String file_id = packet.getFileID() + "#" + packet.getChunkN();

    ConcurrentHashMap<String, Handler> chunks = this.signals.get(packet.getType());
    if (chunks != null) {
      Handler task = chunks.get(file_id);
      if (task != null) {
        task.signal(packet);
      }
    }
  }

  /**
   * Sends a message to the {@link Net_IO} channel
   * @param  packet Message to be sent to channel
   * @return        Whether the message was sent successfully or not
   * This function is never called by the main {@link ChannelListener#run()} thread, only by {@link Handler#run()}
   */
  public boolean sendMsg(PacketInfo packet) {
    return this.channel.sendMsg(packet);
  }

  /**
   * Registers the given {@link Handler} to be notified of certain received messages
   * @param task {@link Handler} to be notified
   * The decision of whether the {@link Handler} needs to be notified or not is in each implementation of {@link Handler}
   * This method is only called by the main {@link ChannelListener#run()} thread, for Handlers which immediatelly know what messages they want to be notified of
   */
  public static void registerForSignal(Pair<String, Handler> task) {
    if (task != null) {
      ConcurrentHashMap<String, Handler> type = signals.computeIfAbsent(task.second().signalType(), (x)->{
        return new ConcurrentHashMap<String, Handler>();
      });
      if (type != null) {
        type.put(task.first(), task.second());
      }
      else {
        System.err.println("Why the frick is type null??");
      }
    }
  }

  /**
   * Registers the given {@link Handler} to be notified of certain received messages
   * @param signal_type Type of message to be signalled
   * @param chunk_id    ID of the chunk (<fileID>#<chunk_number>)
   * @param task        {@link Handler} that wants to be notified of received message
   * This method is only called by {@link Handler#run()}
   */
  public static void registerForSignal(String signal_type, String chunk_id, Handler task) {
    if (chunk_id != null&& signal_type != null&& task != null) {
      ConcurrentHashMap<String, Handler> type = signals.computeIfAbsent(signal_type, (x)->{
        return new ConcurrentHashMap<String, Handler>();
      });
      if (type != null) {
        type.put(chunk_id, task);
      }
      else {
        System.err.println("Why the frick is type null??");
      }
    }
  }

  /**
   * Removes the {@link Handler} from {@link ChannelListener#signals}
   * @param signal_type Type of message the {@link Handler} was being notified
   * @param chunk_id    ID of the chunk {@link Handler} was being notified (<fileID>#<chunk_number>)
   * This method is only called by {@link Handler#run()}
   */
  public static void removeFromSignal(String signal_type, String chunk_id) {
    ConcurrentHashMap type = signals.get(signal_type);

    if (type != null) {
      type.remove(chunk_id);
    }
  }
}
