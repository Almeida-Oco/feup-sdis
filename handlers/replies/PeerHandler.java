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
    this.peer_hash       = hash;
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
  public void run(Packet packet, PacketChannel buffer) {
    System.out.println("Got a peer '" + packet.getHash() + "'");
    long sender_hash = packet.getHash();

    if (this.peer_hash == sender_hash && this.code_executor) {
      String[] ip_port = packet.getIP_Port().split(" ");
      this.handleCodeExecutor(ip_port[0], Integer.parseInt(ip_port[1]));
    }
    else {
      if (this.redirect_buffer != null) {
        this.redirect_buffer.sendPacket(packet);
      }
      else {
        long hash = packet.getHash();
        if (hash != this.peer_hash) { //My predecessor
          this.node.setPredecessor(packet.getIP_Port(), hash, buffer);
        }
        else {
          this.node.addPeer(packet.getIP_Port(), packet.getHash(), buffer);
        }
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

  @Override
  public void run() {
  }
}
