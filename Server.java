import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class Server {
final int DELAY = 5;
int serv_port;
DatagramSocket serv_socket;

InetAddress mcast_addr;
int mcast_port;
MulticastSocket mcast_socket;
byte[] mcast_msg;

// java Server <srvc_port> <mcast_addr> <mcast_port>

public static void main(String[] args) {
  if (!argsCorrect(args)) {
    return;
  }

  Server server = new Server(Integer.parseInt(args[0]));

  server.setupMulticast(args[1], Integer.parseInt(args[2]));
  server.setTimer();
  server.recvMsg();
}

private static boolean argsCorrect(String[] args) {
  int size = args.length;

  if (size == 3) {
    if (!Client.isInteger(args[0])) {
      System.err.println("Server port is NaN!");
      return false;
    }
    if (!Client.isInteger(args[2])) {
      System.err.println("Multicast port is NaN!");
      return false;
    }

    return true;
  } else {
    System.err.println("Usage:\njava Server <srvc_port> <mcast_addr> <mcast_port>");
    return false;
  }
}

public Server(int port_number) {
  this.serv_port = port_number;
  this.serv_socket = null;
  try {
    this.serv_socket = new DatagramSocket(this.serv_port);
    this.mcast_msg = (InetAddress.getLocalHost().getHostAddress() + ":" + this.serv_port).getBytes();
  }
  catch (UnknownHostException err) {
    System.err.println("Failed to get local IP!\n " + err.getMessage());
    return;
  }
  catch (SocketException err) {
    System.err.println("Failed to create UDP socket!\n " + err.getMessage());
    return;
  }

  this.mcast_addr = null;
  this.mcast_port = 0;
  this.mcast_socket = null;
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
    this.mcast_socket.setTimeToLive(1);
  }
  catch (IOException err) {
    System.err.println("Failed to join multicast group: '" + this.mcast_addr.getHostAddress() + "'\n " + err.getMessage());
    return false;
  }

  return true;
}

private void recvMsg() {
  byte[] buf = new byte[256];
  DatagramPacket packet = new DatagramPacket(buf, buf.length);

  System.out.println("Receiving messages");
  try {
    while (true) {
      this.serv_socket.receive(packet);
      String str_recv = new String(packet.getData()).trim();
      System.out.println("Got this: '" + str_recv + "'");
    }
  }
  catch (IOException err) {
    System.err.println("Failed to receive message!\n " + err.getMessage());
    return;
  }
}

private boolean sendServInfo() {
  DatagramPacket packet = new DatagramPacket(this.mcast_msg, this.mcast_msg.length, this.mcast_addr, this.mcast_port);

  try {
    this.serv_socket.send(packet);
  }
  catch (IOException err) {
    System.err.println("Failed to send server info!\n " + err.getMessage());
    return false;
  }
  return true;
}

private void setTimer() {
  Server serv = this;
  Runnable task = new Runnable() {
    public void run() {
      serv.sendServInfo();
    }

  };

  ScheduledExecutorService scheduler
    = Executors.newSingleThreadScheduledExecutor();

  scheduler.scheduleAtFixedRate(task, 0, DELAY, TimeUnit.SECONDS);
}

}
