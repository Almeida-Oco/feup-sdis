package network;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

class Net_IO {
  final int TTL = 1;
  final int BUF_SIZE = 70000;

  DatagramPacket packet;
  MulticastSocket mcast_socket;
  int mcast_port;

  Net_IO(InetAddress addr, int port) {
    this.packet = new DatagramPacket(new byte[BUF_SIZE], BUF_SIZE);
    this.mcast_port = port;
    try {
      this.mcast_socket = new MulticastSocket(port);
    }
    catch (IOException err) {
      System.err.println("Failed to create Multicast Socket!\n " + err.getMessage());
      this.mcast_socket = null;
      return;
    }

    try {
      this.mcast_socket.joinGroup(addr);
      this.mcast_socket.setTimeToLive(TTL);
    }
    catch (IOException err) {
      System.err.println("Failed to join Multicast group: '" + addr.getHostAddress() + "'\n " + err.getMessage());
      this.mcast_socket = null;
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
      System.err.println("Failed to receive message!\n " + err.getMessage());
      return null;
    }

    return PacketInfo.fromString(new String(this.packet.getData(), StandardCharsets.US_ASCII));
  }

  public boolean sendMsg(PacketInfo packet, InetAddress addr, int port) {
    if (!packet.isReady()) {
      System.err.println("Tried to send not ready packet!");
      return false;
    }

    this.packet.setData(packet.toString().getBytes());
    this.packet.setAddress(addr);
    this.packet.setPort(port);
    try {
      this.mcast_socket.send(this.packet);
      return true;
    }
    catch (IOException err) {
      System.err.println("Failed to send DatagramPacket!\n " + err.getMessage());
      return false;
    }
  }
}
