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
  final int BUF_SIZE = 65000;

  MulticastSocket mcast_socket;
  InetAddress mcast_addr;
  int mcast_port;

  public Net_IO(String addr, int port) {
    this.mcast_port = port;
    try {
      this.mcast_socket = new MulticastSocket(port);
      this.mcast_socket.setReceiveBufferSize(BUF_SIZE);
      this.mcast_socket.setSendBufferSize(BUF_SIZE);
    }
    catch (IOException err) {
      System.err.println("Failed to create Multicast Socket!\n - " + err.getMessage());
      this.mcast_socket = null;
      return;
    }

    try {
      this.mcast_addr = InetAddress.getByName(addr);
      this.mcast_socket.joinGroup(this.mcast_addr);
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

  public InetAddress getAddr() {
    return this.mcast_addr;
  }

  public int getPort() {
    return this.mcast_port;
  }

  public boolean isReady() {
    return this.mcast_socket != null;
  }

  public PacketInfo recvMsg() {
    try {
      DatagramPacket packet = new DatagramPacket(new byte[BUF_SIZE], BUF_SIZE);
      this.mcast_socket.receive(packet);
      PacketInfo recv_packet = PacketInfo.fromPacket(packet);

      return recv_packet;
    }
    catch (IOException err) {
      System.err.println("Failed to receive message!\n - " + err.getMessage());
      return null;
    }
  }

  public boolean sendMsg(PacketInfo packet) {
    if (!packet.isReady()) {
      System.err.println("Packet is not ready to be sent!");
      return false;
    }
    DatagramPacket dgram_packet = new DatagramPacket(new byte[BUF_SIZE], BUF_SIZE);
    dgram_packet.setAddress(packet.getAddress());
    dgram_packet.setPort(packet.getPort());

    try {
      String data = packet.toString();
      dgram_packet.setData(data.getBytes(StandardCharsets.US_ASCII), 0, data.length());
      this.mcast_socket.send(dgram_packet);
      return true;
    }
    catch (IOException err) {
      System.err.println("Failed to send DatagramPacket!\n - " + err.getMessage());
      return false;
    }
  }
}
