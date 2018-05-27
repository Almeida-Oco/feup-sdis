package handlers.queries;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.chord.TableEntry;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import handlers.replies.PeerHandler;

public class PeerJoinHandler extends Handler {
  long max_hash;

  public PeerJoinHandler(Node node) {
    super(node);
    this.max_hash = node.maxHash();
  }

  @Override
  public void run(Packet packet, PacketChannel reply_channel) {
    String new_peer_id   = packet.getIP_Port();
    long   new_peer_hash = packet.getHash();

    this.handleSenderPeer(new_peer_id, new_peer_hash, reply_channel);

    String peers_raw = packet.getCode();
    if (peers_raw != null) {
      String[] peers = peers_raw.split(" ");
      this.handleRequestedPeers(peers, reply_channel);
      System.out.println("Sent requested nodes");
    }
  }

  private void handleSenderPeer(String sender_id, long sender_hash, PacketChannel reply_channel) {
    TableEntry    curr          = this.node.getResponsiblePeer(sender_hash);
    long          curr_hash     = curr.getHash();
    PacketChannel curr_channel  = curr.getChannel();
    String        curr_hash_str = Long.toString(curr_hash);

    if (curr_hash < sender_hash) { //Warn curr of new peer appearance
      if (curr_channel != null) {  //Not my sucessor
        curr_channel.sendPacket(Packet.newNewPeerPacket(curr_hash_str, curr.getID(), null));
      }
      else { //My sucessor
        this.node.addPeer(sender_id, sender_hash, reply_channel);
        reply_channel.sendPacket(Packet.newPeerPacket(Long.toString(sender_hash), curr.getID()));
      }
    }
    else if (curr_hash > sender_hash) { //Curr gets taken over by sender and warn curr of sender existance
      this.node.takeOverResponsibility(sender_id, sender_hash, reply_channel);
      reply_channel.sendPacket(Packet.newNewPeerPacket(curr_hash_str, curr.getID(), null));
    }
    else {
      System.out.println("Damn that's nearly impossible!");
      System.exit(666);
    }
  }

  private void handleRequestedPeers(String[] peers, PacketChannel reply_channel) {
    for (String hash_str : peers) {
      long          peer_hash           = Long.parseLong(hash_str);
      TableEntry    responsible         = this.node.getResponsiblePeer(peer_hash);
      PacketChannel responsible_channel = responsible.getChannel();

      if (peer_hash >= this.max_hash || responsible_channel != null) {   //Not my responsibility
        Handler handler = new PeerHandler(this.node, reply_channel, peer_hash);
        PacketDispatcher.registerHandler(Packet.PEER, peer_hash, handler);
        responsible_channel.sendPacket(Packet.newGetPeerPacket(hash_str));
      }
      else {
        String my_id = this.node.getID();
        reply_channel.sendPacket(Packet.newPeerPacket(hash_str, my_id));
      }
    }
  }
}
