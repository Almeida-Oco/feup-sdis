package network.comms;

import java.nio.channels.SocketChannel;
import network.comms.sockets.SSLSocketChannel;

public class PacketBuffer implements Runnable {
  private SSLSocketChannel channel;
  private String channel_id;

  private String built_msg;

  public PacketBuffer(SSLSocketChannel channel) {
    this.channel    = channel;
    this.channel_id = channel.getID();
    this.built_msg  = "";
  }

  public SSLSocketChannel getSocket() {
    return this.channel;
  }

  public SocketChannel getChannel() {
    return this.channel.getChannel();
  }

  public String getChannelID() {
    return this.channel_id;
  }

  @Override
  public void run() {
    String msg = this.channel.recvMsg();

    if (msg != null) {
      this.built_msg += msg;
      int expected_size = this.readMsgSize(this.built_msg);

      if (expected_size != -1 && this.built_msg.length() >= expected_size) { // A message is ready
        String packet_str = this.built_msg.substring(0, expected_size);
        this.built_msg = this.built_msg.substring(expected_size, this.built_msg.length());
        Packet packet = Packet.fromString(packet_str);

        this.sendMsgUpstream(packet);
      }
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

  private void sendMsgUpstream(Packet packet) {
    if (packet != null) {
      System.out.println("GOT '" + packet.toString() + "'");
      System.out.println("Sending packet upstream!");
    }
    else {
      System.out.println("Packet is null!");
    }
  }
}
