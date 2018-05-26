package network.comms;

import java.nio.channels.SocketChannel;
import network.comms.sockets.SSLSocketChannel;

import network.chord.Node;

public class PacketBuffer implements Runnable {
  private static Node myself;
  private SSLSocketChannel channel;

  private String built_msg;

  public PacketBuffer(SSLSocketChannel channel) {
    this.channel   = channel;
    this.built_msg = "";
  }

  public SSLSocketChannel getSocket() {
    return this.channel;
  }

  public SocketChannel getChannel() {
    return this.channel.getChannel();
  }

  public boolean sendPacket(Packet packet) {
    System.out.println("Sending '" + packet.toString() + "'");
    return this.channel.sendMsg(packet.toString());
  }

  @Override
  public void run() {
    String msg = this.channel.recvMsg();

    if (msg != null) {
      this.built_msg += msg.trim();
      int expected_size = this.readMsgSize(this.built_msg);
      System.out.println("Expected = " + expected_size + ", got = " + this.built_msg.length());
      if (expected_size != -1 && this.built_msg.length() >= expected_size) { // A message is ready
        String packet_str = this.built_msg.substring(0, expected_size);
        this.built_msg = this.built_msg.substring(expected_size, this.built_msg.length());
        Packet packet = Packet.fromString(packet_str);

        this.sendMsgUpstream(packet);
      }
    }
  }

  public static void setMyself(Node node) {
    PacketBuffer.myself = node;
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

  private void sendMsgUpstream(Packet packet) {
    if (packet != null) {
      System.out.println("Sending '" + packet.toString() + "' upstream!");
    }
    else {
      System.out.println("Packet is null!");
    }
  }
}
