import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

public class Client {
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
  if (!client.setupMulticast(args[0], Integer.parseInt(args[1]))) {
    return;
  }
  System.out.println(client.recvMsg(client.mcast_socket));

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
  this.msg = (oper + opnd).getBytes();
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

public boolean setupConnection(String serv_addr, int serv_port) {
  this.serv_port = serv_port;
  try {
    this.serv_socket = new DatagramSocket(this.serv_port);
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

public boolean sendMsg(DatagramSocket socket, String msg) {
  byte[] msg_bytes = msg.getBytes();
  boolean sent_message = false;
  InetAddress addr;
  DatagramPacket packet = new DatagramPacket(msg_bytes, msg_bytes.length, this.serv_addr, this.serv_port);

  for (int i = 0; i < 3; i++) {
    try {
      socket.send(packet);
      System.out.println("SENT! '" + msg + "'");
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
    return null;
  }

  return new String(packet.getData(), StandardCharsets.UTF_8);
}

protected void finalize() throws Throwable {
  this.mcast_socket.leaveGroup(this.mcast_addr);
  this.mcast_socket.close();
  this.serv_socket.close();
}

}
