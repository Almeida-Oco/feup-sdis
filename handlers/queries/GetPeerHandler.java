package handlers.queries;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.chord.TableEntry;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import handlers.replies.PeerHandler;

public class GetPeerHandler extends Handler {
  public GetPeerHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel reply_channel) {
    long       peer_hash   = packet.getHash();
    String     hash_str    = Long.toString(peer_hash);
    TableEntry responsible = this.node.getResponsiblePeer(peer_hash);

    if (responsible != null) { // peer in my range
      PacketChannel responsible_channel = responsible.getChannel();

      if (responsible_channel != null) { // Someone else sucessor
        Handler handler = new PeerHandler(this.node, reply_channel, peer_hash);
        PacketDispatcher.registerHandler(Packet.PEER, peer_hash, handler);
        responsible_channel.sendPacket(Packet.newGetPeerPacket(hash_str));
      }
      else {
        System.out.println("My sucessor '" + hash_str + "'");
        reply_channel.sendPacket(Packet.newPeerPacket(hash_str, this.node.getID()));
      }
    }
    else {
      TableEntry last_entry = this.node.lastEntry();
      if (last_entry.getResponsibleHash() != this.node.getHash()) { //Last entry might know
        Handler handler = new PeerHandler(this.node, reply_channel, peer_hash);
        PacketDispatcher.registerHandler(Packet.PEER, peer_hash, handler);
        last_entry.getChannel().sendPacket(Packet.newGetPeerPacket(hash_str));
      }
    }
  }
}
