package network.comms;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.nio.channels.SocketChannel;
import network.comms.sockets.SSLSocketChannel;

import network.chord.Node;
import handlers.PacketDispatcher;

// TODO handle case when multiple packets are being sent
public class PacketChannel implements Runnable {
  private SSLSocketChannel channel;
  private LinkedList<Packet> packet_buffer;
  private String built_msg;

  public PacketChannel(SSLSocketChannel channel) {
    this.channel       = channel;
    this.packet_buffer = new LinkedList<Packet>();
    this.built_msg     = "";
  }

  public static PacketChannel newChannel(String addr, int port) {
    SSLSocketChannel channel = SSLSocketChannel.newChannel(addr, port, true);

    if (channel != null) {
      return new PacketChannel(channel);
    }
    else {
      return null;
    }
  }

  public SSLSocketChannel getSocket() {
    return this.channel;
  }

  public SocketChannel getChannel() {
    return this.channel.getChannel();
  }

  /**
   * Sends a packet to the channel
   * @param  packet Packet to be sent
   * @return        Whether it was succesfully sent or not
   */
  public boolean sendPacket(Packet packet) {
    SSLSocketListener.waitForWrite(this);
    synchronized (this.packet_buffer) {
      this.packet_buffer.add(packet);
    }
    return true;
  }

  void send() {
    synchronized (this.packet_buffer) {
      if (this.packet_buffer.peek() != null) {
        String packet = this.packet_buffer.remove().toString();
        System.out.println("Sending '" + packet + "' --> " + this.getID());
        this.channel.sendMsg(packet);
      }
    }
  }

  public String getID() {
    return this.channel.getID();
  }

  /**
   * Reads a packet from the channel
   */
  @Override
  public void run() {
    String msg = this.channel.recvMsg();

    if (msg != null || !this.built_msg.isEmpty()) {
      if (msg != null) {
        this.built_msg += msg.trim();
      }
      int expected_size = this.readMsgSize(this.built_msg);
      if (expected_size != -1 && this.built_msg.length() >= expected_size) {   // A message is ready
        String packet_str = this.built_msg.substring(0, expected_size);
        this.built_msg = this.built_msg.substring(expected_size);
        Packet packet = Packet.fromString(packet_str);
        if (packet != null) {
          PacketDispatcher.handlePacket(packet, this);
        }
      }
    }
  }

  public boolean isConnected() {
    String msg = this.channel.recvMsg();

    if (msg == null) {
      return false;
    }
    else {
      this.built_msg += msg.trim();
      return true;
    }
  }

  private int readMsgSize(String msg) {
    int begin = this.built_msg.indexOf("\\"), end = this.built_msg.indexOf("/");

    if (begin == -1 || end == -1) {
      return -1;
    }
    else {
      try {
        return Integer.parseInt(this.built_msg.substring(begin + 1, end));
      }
      catch (NumberFormatException err) {
        System.err.println("Message size NaN!\n - " + err.getMessage());
        System.exit(1);
      }
    }
    return -1;
  }
}
