package handlers.queries;

import java.util.Arrays;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.chord.TableEntry;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import handlers.replies.PeerHandler;
import network.comms.SSLSocketListener;

public class PeerJoinHandler extends Handler {
  long max_hash;

  public PeerJoinHandler(Node node) {
    super(node);
    this.max_hash = Node.MAX_ID;
  }

  @Override
  public void run(Packet packet, PacketChannel reply_channel) {
    String new_peer_id   = packet.getIP_Port();
    long   new_peer_hash = packet.getHash();

    if (!this.handleSenderPeer(new_peer_id, new_peer_hash, reply_channel)) { // Peer is not my sucessor
      System.out.println("----------------->Not my successor!!!");
      String peers_raw = packet.getCode();
      if (peers_raw != null) {
        String[] peers = peers_raw.split(" ");
        this.handleRequestedPeers(peers, new_peer_id, new_peer_hash, reply_channel);
      }
    }
  }

  /** Returns whether the sender peer is my successor or not */
  private boolean handleSenderPeer(String sender_id, long sender_hash, PacketChannel reply_channel) {
    TableEntry responsible = this.node.getResponsiblePeer(sender_hash);

    if (responsible != null&& responsible.getID() != sender_id) {
      PacketChannel channel = responsible.getChannel();

      if (channel == null) {
        System.out.println("  My resposibility");
        PacketChannel peer_channel = this.chooseChannel(sender_id, reply_channel);
        if (peer_channel != null) {
          this.node.addPeer(sender_id, sender_hash, peer_channel);
          String id = this.node.getID();
          if (this.node.getPredecessor() == null) {
            this.node.setPredecessor((id = sender_id), sender_hash, peer_channel);
          }
          peer_channel.sendPacket(Packet.newPeerPacket(Long.toString(sender_hash), id));
          return true;
        }
        System.out.println("PeerJoinHandler == null ???");
      }
      else {
        long          curr_hash    = responsible.getResponsibleHash();
        PacketChannel curr_channel = responsible.getChannel();

        if (curr_hash < sender_hash) {
          curr_channel.sendPacket(Packet.newNewPeerPacket(Long.toString(sender_hash), sender_id, null));
          return true;
        }
        else if (curr_hash > sender_hash) {
          reply_channel.sendPacket(Packet.newPeerPacket(Long.toString(sender_hash), responsible.getID()));
          responsible.updateInfos(sender_id, sender_hash, reply_channel);
          return true;
        }
      }
    }
    return false;
  }

  private void handleRequestedPeers(String[] peers, String sender_id, long sender_hash, PacketChannel reply_channel) {
    long my_hash = this.node.getHash();
    int  size    = peers.length;

    for (int i = 0; i < size; i++) {
      String     hash_str    = peers[i];
      long       peer_hash   = Long.parseLong(hash_str);
      TableEntry responsible = this.node.getResponsiblePeer(peer_hash);


      if (responsible != null&& responsible.getID() != sender_id) { //Redirect query to responsible peer
        PacketChannel channel = responsible.getChannel();
        if (channel == null) {                                      // Im responsible for it
          PacketChannel peer_channel = this.chooseChannel(sender_id, reply_channel);
          this.node.addPeer(sender_id, peer_hash, peer_channel);
          peer_channel.sendPacket(Packet.newPeerPacket(hash_str, this.node.getID()));
        }
        else {                                                      // One of my sucessors is responsible for it
          String[] rem_peers = Arrays.copyOfRange(peers, i, size);
          Packet   packet    = Packet.newNewPeerPacket(Long.toString(sender_hash), sender_id, rem_peers);

          channel.sendPacket(packet);
          break;
        }
      }
    }
  }
}
