package handlers.replies;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;

public class PeerHandler extends Handler {
  long peer_hash;
  PacketChannel redirect_buffer;
  boolean code_executor;
  String file_content;

  public PeerHandler(Node node, PacketChannel redirect_buffer, long hash) {
    super(node);

    this.redirect_buffer = redirect_buffer;
    this.peer_hash       = node.getHash();
    this.code_executor   = false;
    this.file_content    = null;
  }

  public PeerHandler(Node node, long code_hash, String file_content) {
    super(node);

    this.redirect_buffer = null;
    this.peer_hash       = code_hash;
    this.code_executor   = true;
    this.file_content    = file_content;
  }

  @Override
  public void run(Packet packet, PacketChannel reply_channel) {
    System.out.println("Got a peer '" + packet.getHash() + "'");
    long sender_hash = packet.getHash();

    PacketDispatcher.unregisterHandler(Packet.PEER, sender_hash);
    if (this.peer_hash == sender_hash && this.code_executor) {
      String[] ip_port = packet.getIP_Port().split(" ");
      this.handleCodeExecutor(ip_port[0], Integer.parseInt(ip_port[1]));
    }
    else {
      if (redirect_buffer == null) {
        this.handleRegularPeerMsg(packet.getIP_Port(), sender_hash, reply_channel);
      }
      else {
        redirect_buffer.sendPacket(packet);
      }
    }
  }

  private void handleCodeExecutor(String addr, int port) {
    PacketChannel channel = PacketChannel.newChannel(addr, port);

    if (channel != null) {
      PacketDispatcher.registerHandler(Packet.RESULT, this.peer_hash, new CodeResultHandler(this.node));
      channel.sendPacket(Packet.newCodePacket(Long.toString(this.peer_hash), this.file_content));
    }
    else {
      System.err.println("Supplied '" + addr + ":" + port + "', but could not find peer!");
    }
  }

  private void handleRegularPeerMsg(String sender_id, long sender_hash, PacketChannel reply_channel) {
    if (sender_hash == this.peer_hash) { // Some update regarding my sucessor/predecessor
      if (sender_id.equals(this.node.getID())) {
        this.node.setPredecessor(reply_channel);
      }
      else {
        String[]      ips = sender_id.split(":");
        PacketChannel sucessor_channel = PacketChannel.newChannel(ips[0], Integer.parseInt(ips[1]));
        if (sucessor_channel != null) {
          this.node.addPeer(sender_id, Node.hash(sender_id.getBytes()), sucessor_channel);
        }
        else {
          System.out.println("Failed to connect to sucessor given by '" + sender_hash + "'");
        }
      }
    }
    else { //Updates regarding my sucessors
      String[]      ips = sender_id.split(":");
      PacketChannel sucessor_channel = PacketChannel.newChannel(ips[0], Integer.parseInt(ips[1]));
      if (sucessor_channel != null) {
        System.out.println("HASH = '" + sender_hash + "'");
        this.node.addPeer(sender_id, sender_hash, sucessor_channel);
      }
      else {
        System.out.println("Failed to connect to sucessor given by '" + sender_hash + "'");
      }
    }
  }
}
