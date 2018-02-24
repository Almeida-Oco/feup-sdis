import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

public class Client {
final static int PORT = 8080;
InetAddress mcast_addr;
int mcast_port;
MulticastSocket mcast_socket;

InetAddress serv_addr;
int serv_port;
DatagramSocket serv_socket;

byte[] msg;

public static void main(String[] args) throws IOException {
  if (!argsCorrect(args)) {
    return;
  }
  Client client = new Client(args[2], args[3]);
  if (!client.setupMulticast(args[0], Integer.parseInt(args[1])) || !client.setupUDP()) {
    return;
  }
  client.sendMsg(client.serv_socket, client.msg);
  System.out.println(client.recvMsg(client.serv_socket));
}

private static boolean argsCorrect(String[] args) {
  int size = args.length;

  if (size < 4 || size > 5) {
    System.err.println("Usage:\n  java Client <mcast_address> <mcast_port> <oper> <opnd>*");
    return false;
  } else if (!isInteger(args[1])) {
    System.err.println("Port number NaN!");
    return false;
  }
  if (size == 5) {
    if (args[4].length() > 255) {
      System.err.println("Owner name too big!");
      return false;
    }
    args[3] += " " + args[4];
  }

  return true;
}

public static boolean isInteger(String str) {
  int size = str.length();

  for (int i = 0; i < size; i++) {
    if (!Character.isDigit(str.charAt(i))) {
      return false;
    }
  }

  return size > 0;
}

public Client(String oper, String opnd) {
  this.msg = (oper.toUpperCase() + " " + opnd.toUpperCase()).getBytes();
  this.mcast_addr = null;
  this.mcast_port = 0;
  this.mcast_socket = null;
  this.serv_addr = null;
  this.serv_port = 0;
  this.serv_socket = null;
}

public boolean setupMulticast(String mcast_addr, int mcast_port) {
  this.mcast_port = mcast_port;
  try {
    this.mcast_socket = new MulticastSocket(this.mcast_port);
    this.mcast_addr = InetAddress.getByName(mcast_addr);
  }
  catch (UnknownHostException err) {
    System.err.println("Failed to find IP of '" + mcast_addr + "'\n " + err.getMessage());
    return false;
  }
  catch (IOException err) {
    System.err.println("Failed to create Multicast UDP socket!\n " + err.getMessage());
    return false;
  }

  try {
    this.mcast_socket.joinGroup(this.mcast_addr);
  }
  catch (IOException err) {
    System.err.println("Failed to join multicast group: '" + this.mcast_addr.getHostAddress() + "'\n " + err.getMessage());
    return false;
  }
  return true;
}

public boolean setupUDP() {
  String[] serv_info = this.recvMsg(this.mcast_socket).trim().split(":");
  if (serv_info.length != 2) {
    return false;
  }

  return this.setupConnection(serv_info[0], Integer.parseInt(serv_info[1]));
}

private boolean setupConnection(String serv_addr, int serv_port) {
  this.serv_port = serv_port;
  try {
    this.serv_socket = new DatagramSocket(PORT);
    this.serv_addr = InetAddress.getByName(serv_addr);
  }
  catch (UnknownHostException err) {
    System.err.println("Failed to find IP of '" + serv_addr + "'\n " + err.getMessage());
    return false;
  }
  catch (IOException err) {
    System.err.println("Failed to create UDP socket!\n " + err.getMessage());
    return false;
  }
  return true;
}

public boolean sendMsg(DatagramSocket socket, byte[] msg) {
  DatagramPacket packet = new DatagramPacket(msg, msg.length, this.serv_addr, this.serv_port);

  for (int i = 0; i < 3; i++) {
    try {
      socket.send(packet);
      return true;
    }
    catch (IOException err) {
      System.err.println("Failed to send DatagramPacket: " + err.getMessage() + "\nRetrying in 2 sec...");
      try {
        TimeUnit.SECONDS.sleep(2);
      }
      catch (Throwable err2) {
        System.err.println("Failed to sleep for 2 sec, exiting...");
        return false;
      }
    }
  }

  return false;
}

public String recvMsg(DatagramSocket socket) {
  byte[] buf = new byte[256];
  DatagramPacket packet = new DatagramPacket(buf, buf.length);
  boolean received = false;

  for (int i = 0; i < 2 && !received; i++) {
    try {
      socket.receive(packet);
      received = true;
    }
    catch (IOException err) {
      System.err.println("Failed to receive message: " + err.getMessage() + "\nRetrying...");
    }
  }
  if (!received) {
    return "";
  }

  return new String(packet.getData(), StandardCharsets.UTF_8);
}

protected void finalize() throws Throwable {
  this.mcast_socket.leaveGroup(this.mcast_addr);
  this.mcast_socket.close();
  this.serv_socket.close();
}

}
