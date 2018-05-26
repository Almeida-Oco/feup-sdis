import java.lang.Thread;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.net.InetSocketAddress;

import network.chord.Node;
import network.comms.SSLSocketListener;
import network.comms.sockets.SSLSocketChannel;
import network.comms.sockets.SSLServerSocketChannel;

class Service {
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

    Node myself = new Node(remote_channel, new InetSocketAddress(local_port));
    SSLSocketListener listener = new SSLSocketListener(myself);

    listener.waitForAccept(serv_channel.getSocket());

    try {
      if (remote_channel != null) {
        System.out.println("Sending msg");
        remote_channel.sendMsg("\\42/ NEW_PEER abcdefghijklmnopqrstuvwxyz\r\n");
      }
      listener.listen();
    }
    catch (IOException err) {
      System.err.println("Error while listening sockets!\n - " + err.getMessage());
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
