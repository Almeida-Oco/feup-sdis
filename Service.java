import java.lang.Thread;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.net.InetSocketAddress;

import network.comms.sockets.SSLSocketChannel;
import network.comms.sockets.SSLServerSocketChannel;

class Service {
  public static void main(String[] args) {
    if (!Service.validArgs(args)) {
      return;
    }
    String   service = args[1];
    String[] ip_port = args[0].split(":");
    String   ip      = ip_port[0];
    int      port    = Integer.parseInt(ip_port[1]);
    boolean  client  = service.equalsIgnoreCase("client");


    if (client) {
      SSLSocketChannel socket = SSLSocketChannel.newChannel(ip, port);
      System.out.println("Sending message");
      boolean sent = socket.sendMsg("BEBI *******");
      if (sent) {
        System.out.println("Message sent!");
        String msg;
        while ((msg = socket.recvMsg()) == null) {
        }
        System.out.println("Response = '" + msg + "'");
      }
      else {
        System.out.println("Message not sent!");
      }
    }
    else {
      SSLServerSocketChannel channel = SSLServerSocketChannel.newChannel(ip, port);
      SSLSocketChannel       socket  = channel.accept();
      String msg;
      if (socket == null) {
        System.out.println("socket is null");
        return;
      }
      else {
        System.out.println("Socket is not null!");

        while ((msg = socket.recvMsg()) == null) {
        }

        socket.sendMsg("GOT IT");
      }
      System.out.println("GOT '" + msg + "'");
    }
  }

  private static boolean validArgs(String[] args) {
    if (args.length != 2) {
      System.err.println("Wrong args!\n  java Service <ip>:<port> <service>");
      return false;
    }

    if (args[0].indexOf(':') == -1) {
      System.err.println("Wrong format! <ip>:<port> \n  java Service <ip>:<port>");
    }


    if (!args[1].equalsIgnoreCase("client") && !args[1].equalsIgnoreCase("server")) {
      System.err.println("Wrong service!\n (client/server)");
      return false;
    }

    return true;
  }
}
