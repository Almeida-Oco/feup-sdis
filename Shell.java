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
import network.comms.Packet;
import handlers.SynchronizeHandler;
import network.comms.SSLSocketListener;
import network.comms.sockets.SSLSocketChannel;
import network.comms.sockets.SSLServerSocketChannel;

import worker.*;

class Shell {

  public static void main(String[] args) {
    if (!Shell.validArgs(args)) {
      return;
    }
    int      local_port = Integer.parseInt(args[1]);
    String[] ip_port    = args[0].split(":");
    String   ip         = ip_port[0];
    int      port       = Integer.parseInt(ip_port[1]);
    String   command    = args[2];

    Shell.start(local_port, ip, port, command);
  }

private static void start(int local_port, String remote_ip, int remote_port, String command) {
    SSLSocketChannel       remote_channel = SSLSocketChannel.newChannel(remote_ip, remote_port, true);
    SSLServerSocketChannel serv_channel   = SSLServerSocketChannel.newChannel(local_port);
    PacketChannel pack_channel = new PacketChannel(remote_channel);
    //Node myself = new Node(remote_channel, local_port);
    //SSLSocketListener listener = new SSLSocketListener(myself);
    String hellocode = "public class HelloWorld { public static void main(String[] args) { System.out.println(\"Hello world from example program \"); } }";
    PacketChannel comms_channel = new PacketChannel(remote_channel);
    comms_channel.sendPacket(Packet.newCodePacket(Worker.hash(hellocode), hellocode));
}

  private static boolean validArgs(String[] args) {
    if (args.length < 3) {
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
    System.err.println("  java Shell <remote_ip>:<remote_port> <local_port> <command>");
  }
}