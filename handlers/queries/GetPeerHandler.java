package handlers.queries;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.chord.TableEntry;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import handlers.replies.PeerHandler;

public class GetPeerHandler extends Handler {
  long max_hash;

  public GetPeerHandler(Node node) {
    super(node);
    this.max_hash = this.node.maxHash();
  }

  @Override
  public void run(Packet packet, PacketChannel reply_channel) {
    long          requested_hash      = packet.getHash();
    String        hash_str            = Long.toString(requested_hash);
    TableEntry    responsible         = this.node.getResponsiblePeer(requested_hash);
    PacketChannel responsible_channel = responsible.getChannel();


    // Not my responsibility
    if (requested_hash >= this.max_hash || responsible_channel != null) {
      Handler handler = new PeerHandler(this.node, reply_channel, requested_hash);
      PacketDispatcher.registerHandler(Packet.PEER, requested_hash, handler);
      responsible_channel.sendPacket(Packet.newGetPeerPacket(hash_str));
    }
    else {
      String my_id = this.node.getID();
      reply_channel.sendPacket(Packet.newPeerPacket(hash_str, my_id));
    }
  }
}
