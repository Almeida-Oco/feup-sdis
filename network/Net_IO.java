package network;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.UnknownHostException;
import java.lang.SecurityException;

public class Net_IO {
  final int TTL      = 1;
  final int BUF_SIZE = 70000;

  DatagramPacket packet;
  MulticastSocket mcast_socket;
  int mcast_port;

  public Net_IO(String addr, int port) {
    this.packet     = new DatagramPacket(new byte[BUF_SIZE], BUF_SIZE);
    this.mcast_port = port;
    try {
      this.mcast_socket = new MulticastSocket(port);
    }
    catch (IOException err) {
      System.err.println("Failed to create Multicast Socket!\n - " + err.getMessage());
      this.mcast_socket = null;
      return;
    }

    try {
      this.mcast_socket.joinGroup(InetAddress.getByName(addr));
      this.mcast_socket.setTimeToLive(TTL);
    }
    catch (UnknownHostException err) {
      System.err.println("No host found associated with IP '" + addr + "'\n - " + err.getMessage());
      this.mcast_socket = null; //On fail this is set to null
    }
    catch (SecurityException err) {
      System.err.println("Not allowed to execute checkConnect method!\n - " + err.getMessage());
      this.mcast_socket = null; //On fail this is set to null
    }
    catch (IOException err) {
      System.err.println("Failed to join Multicast group: '" + addr + "'\n - " + err.getMessage());
      this.mcast_socket = null; //On fail this is set to null
    }
  }

  public boolean isReady() {
    return this.mcast_socket != null;
  }

  //TODO should we create a new PacketInfo each time we receive?
  public PacketInfo recvMsg() {
    try {
      this.mcast_socket.receive(this.packet);
    }
    catch (IOException err) {
      System.err.println("Failed to receive message!\n - " + err.getMessage());
      return null;
    }
    System.out.println("Data size = " + this.packet.getLength());
    return PacketInfo.fromPacket(this.packet);
  }

  public boolean sendMsg(PacketInfo packet) {
    if (!packet.isReady()) {
      System.err.println("Packet is not ready to be sent!");
      return false;
    }

    this.packet.setData(packet.toString().getBytes());
    this.packet.setAddress(packet.getAddress());
    this.packet.setPort(packet.getPort());
    try {
      this.mcast_socket.send(this.packet);
      return true;
    }
    catch (IOException err) {
      System.err.println("Failed to send DatagramPacket!\n - " + err.getMessage());
      return false;
    }
  }
}
