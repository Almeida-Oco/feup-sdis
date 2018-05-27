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

    Service.start(local_port, ip, port);
  }

  private static void start(int local_port, String remote_ip, int remote_port) {
    SSLSocketChannel       remote_channel = SSLSocketChannel.newChannel(remote_ip, remote_port, true);
    SSLServerSocketChannel serv_channel   = SSLServerSocketChannel.newChannel(local_port);
    Node myself = new Node(remote_channel, local_port);
    SSLSocketListener listener = new SSLSocketListener(myself);

    PacketDispatcher.initQueryHandlers(myself);
    SSLSocketListener.waitForAccept(serv_channel.getSocket());

    startSynchronizeThread(myself);
    System.out.println("My Hash = " + myself.getHash());
    if (remote_channel != null) {
      System.out.println("Discovered a network!\n - Starting node discovery...");
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
    int    delay = 40 + rand.nextInt(20);

    handler = new SynchronizeHandler(myself);

    try {
      executor.scheduleAtFixedRate(handler, 3, 3, TimeUnit.SECONDS);
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
