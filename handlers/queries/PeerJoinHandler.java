package handlers.queries;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.chord.TableEntry;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import handlers.replies.PeerHandler;

public class PeerJoinHandler extends Handler {
  public PeerJoinHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel buffer) {
    String[] nodes_hash = packet.getCode().split(" ");
    for (String hash_str : nodes_hash) {
      long       hash = Long.parseLong(hash_str);
      TableEntry peer = this.node.getResponsiblePeer(hash);

      if (peer != null) { // Im responsible for packet
        PacketChannel peer_buffer = peer.getChannel();
        if (peer_buffer == null) {
          buffer.sendPacket(Packet.newPeerPacket(hash_str, peer.getID()));
        }
        else {
          Handler handler = new PeerHandler(this.node, buffer, hash);
          PacketDispatcher.registerHandler(Packet.GET_PEER, hash, handler);
          peer_buffer.sendPacket(Packet.newGetPeerPacket(hash_str));
        }
      }
      else {
        System.err.println("How could this happen?");
        System.exit(2);
      }
    }
    buffer.sendPacket(Packet.newPeerPacket(Long.toString(packet.getHash()), this.node.getID()));
    System.out.println("Sent requested nodes");
  }

  @Override
  public void run() {
    System.out.println("Run no args?!");
  }
}
