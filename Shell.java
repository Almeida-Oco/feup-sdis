import java.util.*;
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

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import handlers.SynchronizeHandler;
import handlers.replies.PeerHandler;
import network.comms.SSLSocketListener;
import network.comms.sockets.SSLSocketChannel;
import network.comms.sockets.SSLServerSocketChannel;

import worker.*;

class Shell {
  private static final String SINGLECODE = "CODEONE";
  private static final String MULCODE    = "CODEMUL";

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

  private static void start(int local_port, String remote_ip, int remote_port, String file_name) {
    PacketChannel          remote_channel = PacketChannel.newChannel(remote_ip, remote_port);
    SSLServerSocketChannel serv_channel   = SSLServerSocketChannel.newChannel(local_port);
    Node myself = new Node(remote_channel, local_port);
    SSLSocketListener listener = new SSLSocketListener(myself);


    String[] temp         = { file_name };
    String   program_code = Worker.programsToStrings(temp)[0];

    long hash = Node.hash(program_code.getBytes());

    PacketDispatcher.initQueryHandlers(myself);
    Packet packet = Packet.newGetPeerPacket(Long.toString(hash), myself.getID());
    SSLSocketListener.waitForAccept(serv_channel.getSocket());
    SSLSocketListener.waitForWrite(remote_channel);
    SSLSocketListener.waitForRead(remote_channel);

    Handler handler = new PeerHandler(myself, hash, program_code);
    PacketDispatcher.registerHandler(Packet.PEER, hash, handler);
    remote_channel.sendPacket(packet);
    try {
      listener.listen();
    }
    catch (Exception e) {
    }
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
    System.err.println("  java Shell <remote_ip>:<remote_port> <local_port> <file_name>");
  }
}
