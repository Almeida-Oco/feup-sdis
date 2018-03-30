package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.lang.SecurityException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Handles all network related input and output
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class Net_IO {
  private static final int TTL      = 1;
  private static final int BUF_SIZE = 66000;

  /** Multicast socket to read and write from */
  MulticastSocket mcast_socket;

  /**  Address of multicast socket */
  InetAddress mcast_addr;

  /** Port of multicast socket */
  int mcast_port;

  /**
   * Initializes a new {@link Net_IO}
   * @param addr Address to use
   * @param port Port to use
   */
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

  /**
   * Gets the address associated with the socket
   * @return {@link Net_IO#mcast_addr}
   */
  public InetAddress getAddr() {
    return this.mcast_addr;
  }

  /**
   * Gets the port associated with the socket
   * @return {@link Net_IO#mcast_port}
   */
  public int getPort() {
    return this.mcast_port;
  }

  /** Whether this object is ready or not  */
  public boolean isReady() {
    return this.mcast_socket != null;
  }

  /**
   * Receives a message from {@link Net_IO#mcast_socket}
   * @return Message received, null on error
   */
  public PacketInfo recvMsg() {
    try {
      DatagramPacket packet = new DatagramPacket(new byte[BUF_SIZE], BUF_SIZE);
      System.out.println("Waiting to receive");
      this.mcast_socket.receive(packet);
      System.out.println("Received!");
      PacketInfo recv_packet = PacketInfo.fromPacket(packet);
      System.out.println("Got '" + recv_packet.getType() + "', #" + recv_packet.getChunkN() + ", sender = " + recv_packet.getSenderID());
      return recv_packet;
    }
    catch (IOException err) {
      System.err.println("Failed to receive message!\n - " + err.getMessage());
      return null;
    }
  }

  /**
   * Sends a message to {@link Net_IO#mcast_socket}
   * @param  packet Message to send
   * @return        Whether message was sent or not
   */
  public boolean sendMsg(PacketInfo packet) {
    if (!packet.isReady()) {
      System.err.println("Packet is not ready to be sent!");
      return false;
    }

    String         data         = packet.toString();
    int            size         = data.length();
    DatagramPacket dgram_packet = new DatagramPacket(new byte[size], size, this.mcast_addr, this.mcast_port);

    try {
      dgram_packet.setData(data.getBytes(StandardCharsets.ISO_8859_1), 0, size);
      this.mcast_socket.send(dgram_packet);
      return true;
    }
    catch (IOException err) {
      System.err.println("Failed to send DatagramPacket!\n - " + err.getMessage());
      return false;
    }
  }
}
