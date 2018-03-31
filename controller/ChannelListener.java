package controller;

import network.Net_IO;
import controller.Pair;
import network.PacketInfo;
import controller.Handler;
import controller.ApplicationInfo;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Listens for messages on a given Multicast channel
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class ChannelListener implements Runnable {
  private static final int cores     = Runtime.getRuntime().availableProcessors();
  private static final int MAX_TASKS = 255;


  /** The channel to listen to */
  private Net_IO channel;

  /** Queue to store to be processed received messages */
  private static final ThreadPoolExecutor task_queue;

  static {
    LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(MAX_TASKS);
    task_queue = new ThreadPoolExecutor(cores, cores, 0, TimeUnit.SECONDS, queue);
  }

  /**
   * Initializes the {@link ChannelListener}
   * @param channel Channel to listen to
   * @param tasks   Queue to store to be processed messages
   */
  public ChannelListener(Net_IO channel) {
    this.channel = channel;
  }

  @Override
  public void run() {
    String     txt = null;
    PacketInfo packet;

    do {
      if ((packet = this.channel.recvMsg()) != null&& (packet.getSenderID() != ApplicationInfo.getServID())) {
        System.out.println("Got '" + packet.getType() + "', chunk #" + packet.getChunkN() + ", peer " + packet.getSenderID());

        SignalHandler.addPacket(packet);
        this.task_queue.execute((Runnable)Handler.newHandler(packet));
      }
    } while (txt == null);

    this.task_queue.shutdown();
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
}
