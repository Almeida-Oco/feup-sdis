package handlers.queries;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.chord.TableEntry;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import handlers.replies.PeerHandler;
import network.comms.SSLSocketListener;

public class GetPeerHandler extends Handler {
  public GetPeerHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel reply_channel) {
    long          peer_hash   = packet.getHash();
    String        peer_id     = packet.getIP_Port();
    String        hash_str    = Long.toString(peer_hash);
    TableEntry    responsible = this.node.getResponsiblePeer(peer_hash);
    PacketChannel channel     = responsible.getChannel();

    if (channel == null) {  // Peer is my responsability
      channel = this.chooseChannel(peer_id, reply_channel);
      channel.sendPacket(Packet.newPeerPacket(Long.toString(peer_hash), this.node.getID()));
    }
    else {
      channel.sendPacket(packet);
    }
  }
}
