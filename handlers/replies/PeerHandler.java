package handlers.replies;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketChannel;

public class PeerHandler extends Handler {
  long peer_hash;
  PacketChannel redirect_buffer;

  public PeerHandler(Node node, PacketChannel redirect_buffer, long hash) {
    super(node);
    this.redirect_buffer = redirect_buffer;
    this.peer_hash       = hash;
  }

  @Override
  public void run(Packet packet, PacketChannel buffer) {
    System.out.println("Got a peer '" + packet.getHash() + "'");
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

  @Override
  public void run() {
  }
}
