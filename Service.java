import java.util.Random;
import java.util.Vector;
import java.lang.Thread;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import network.chord.Node;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import handlers.SynchronizeHandler;
import network.comms.SSLSocketListener;
import network.comms.sockets.SSLSocketChannel;
import network.comms.sockets.SSLServerSocketChannel;

class Service {
  private static final int[] sync_time = { 4, 6 };

  private static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
  private static SynchronizeHandler handler;

  public static void main(String[] args) {
    if (!Service.validArgs(args)) {
      return;
    }
    int      local_port = Integer.parseInt(args[1]);
    String[] ip_port    = args[0].split(":");
    String   ip         = ip_port[0];
    int      port       = Integer.parseInt(ip_port[1]);

    Service.setupNetwork(local_port, ip, port);
  }

  private static void setupNetwork(int local_port, String remote_ip, int remote_port) {
    PacketChannel remote_channel = PacketChannel.newChannel(remote_ip, remote_port);

    SSLServerSocketChannel serv_channel = SSLServerSocketChannel.newChannel(local_port);
    Node myself = new Node(remote_channel, local_port);
    SSLSocketListener listener = new SSLSocketListener(myself);

    PacketDispatcher.initQueryHandlers(myself);
    if (serv_channel != null) {
      SSLSocketListener.waitForAccept(serv_channel.getSocket());

      startProgram(myself, listener, remote_channel);
    }
    else {
      System.err.println("Failed to create server socket!");
    }
  }

  private static void startProgram(Node myself, SSLSocketListener listener, PacketChannel channel) {
    startSynchronizeThread(myself);

    System.out.println("My hash = " + myself.getHash());
    if (channel != null) {
      System.out.println("Discovered a network!\n - Starting node discovery...");
      SSLSocketListener.waitForRead(channel);
      if (!myself.startNodeDiscovery()) {
        System.out.println("Failed to start node discovery!\n - Aborting...");
        return;
      }
    }
    else {
      System.out.println("Failed to discover nodes!\n - Starting a new network...");
    }

    try {
      listener.listen();
    }
    catch (IOException err) {
      System.err.println("Error while listening sockets!\n - " + err.getMessage());
    }
  }

  private static void startSynchronizeThread(Node myself) {
    Random rand  = new Random();
    int    delay = sync_time[0] + rand.nextInt(sync_time[1] - sync_time[0]);

    handler = new SynchronizeHandler(myself);

    try {
      executor.scheduleAtFixedRate(handler, delay, delay, TimeUnit.SECONDS);
    }
    catch (Exception err) {
      System.err.println("Synchronized thread interrupted! Aborting...");
      System.exit(1);
    }
  }

  private static boolean validArgs(String[] args) {
    if (args.length != 2) {
      System.err.println("Wrong args!");
      printUsage();
      return false;
    }

    if (args[0].indexOf(':') == -1) {
      System.err.println("Wrong format! <ip>:<port>");
      printUsage();
      return false;
    }

    try {
      Integer.parseInt(args[1]);
      return true;
    }
    catch (NumberFormatException err) {
      System.err.println("Local port NaN!");
      printUsage();
      return false;
    }
  }

  private static void printUsage() {
    System.err.println("  java Service <remote_ip>:<remote_port> <local_port>");
  }
}
