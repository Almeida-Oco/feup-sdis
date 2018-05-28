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

import handlers.replies.*;
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
  private static final String MULCODE = "CODEMUL"; 

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

  private static void start(int local_port, String remote_ip, int remote_port, String command, String[] args) {
    PacketChannel          channel      = PacketChannel.newChannel(remote_ip, remote_port);
    SSLServerSocketChannel serv_channel = SSLServerSocketChannel.newChannel(local_port);
    Node myself = new Node(channel, local_port);
    SSLSocketListener listener = new SSLSocketListener(myself);

    PacketDispatcher.initQueryHandlers(myself);
    String hellocode = "public class HelloWorld { public static void main(String[] args) { System.out.println(\"Hello world from example program \"); } }";


    String protocol = args[2];

    if (protocol.equals(MULCODE)) {
      String[] programs_name = Arrays.copyOfRange(args, 2, args.length);
      String[] programs_code = Worker.programsToStrings(programs_name);
      
      for (String program_code : programs_code) {
   //       comms_channel.sendPacket(Packet.newCodePacket(String.valueOf(Node.hash(program_code.getBytes())), program_code));
      }

    }  else if(protocol.equals(SINGLECODE)){
        String program_name = args[3];
        String[] temp = {program_name}; 
        String program_code = Worker.programsToStrings(temp)[0];
        //String[] prog_args = Arrays.copyOfRange(args, 4, args.length);

        long hash = Node.hash(program_code.getBytes());

        PacketDispatcher.registerHandler(Packet.RESULT, hash, new CodeResultHandler(null));

        channel.sendPacket(Packet.newCodePacket(Long.toString(Node.hash(program_code.getBytes())), program_code));

        SSLSocketListener.waitForRead(channel);
        try {
          listener.listen();
        }
        catch (Exception e) {
          System.err.println("Unable to listen to response from chord service");
        }
  
}

        //comms_channel.sendPacket(Packet.newCodePacket(Worker.hash(prgram_code), program_code));
    else {
      System.out.println("Unknown protocol '" + protocol + "'");
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
    System.err.println("  java Shell <remote_ip>:<remote_port> <local_port> <PROTOCOL>");
    System.err.println("Avaiable protocols: CODEONE [file_name file_name2 ...] Compile and run multiple Programs witho no args");
    System.err.println("Avaiable protocols: CODEMUL file_name [ARGS] Compile and run a single Program with arguments");
  }
}