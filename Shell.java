import java.util.Random;
import java.util.Vector;
import java.util.*;
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


  private static final String SINGLECODE  = "CODEONE"; 
  private static final String MULCODE  = "CODEMUL"; 


  public static void main(String[] args) {
    if (!Shell.validArgs(args)) {
      return;
    }
    int      local_port = Integer.parseInt(args[1]);
    String[] ip_port    = args[0].split(":");
    String   ip         = ip_port[0];
    int      port       = Integer.parseInt(ip_port[1]);
    String   command    = args[2];

    Shell.start(local_port, ip, port, command, args);
  
}

private static void start(int local_port, String remote_ip, int remote_port, String command, String args[]) {
    SSLSocketChannel       remote_channel = SSLSocketChannel.newChannel(remote_ip, remote_port, true);
    SSLServerSocketChannel serv_channel   = SSLServerSocketChannel.newChannel(local_port);
    PacketChannel pack_channel = new PacketChannel(remote_channel);

    PacketChannel comms_channel = new PacketChannel(remote_channel);
    
    String protocol = args[2];

    if (protocol.equals(MULCODE)) {
      String[] programs_name = Arrays.copyOfRange(args, 3, args.length);
      String[] programs_code = Worker.programsToStrings(programs_name);
      
      for (String program_code : programs_code) {
          comms_channel.sendPacket(Packet.newCodePacket(Worker.hash(program_code), program_code));
      }

    }  else if(protocol.equals(SINGLECODE)){
        String program_name = args[3];
        String[] prog_args = Arrays.copyOfRange(args, 4, args.length);
        //comms_channel.sendPacket(Packet.newCodePacket(Worker.hash(prgram_code), program_code));
    }
    else {
      System.out.println("Unknown protocol '" + protocol + "'");
    }
    return;
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
    System.err.println("  java Shell <remote_ip>:<remote_port> <local_port> <PROTOCOL>");
    System.err.println("Avaiable protocols: CODEONE [file_name file_name2 ...] Compile and run multiple Programs witho no args");
    System.err.println("Avaiable protocols: CODEMUL file_name [ARGS] Compile and run a single Program with arguments");
  }
}