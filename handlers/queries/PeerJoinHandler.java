package handlers.queries;

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
    this.max_hash = node.maxHash();
  }

  @Override
  public void run(Packet packet, PacketChannel reply_channel) {
    String new_peer_id   = packet.getIP_Port();
    long   new_peer_hash = packet.getHash();

    System.out.println("NEW_PEER '" + new_peer_id + "'");

    this.handleSenderPeer(new_peer_id, new_peer_hash, reply_channel);

    String peers_raw = packet.getCode();
    if (peers_raw != null) {
      String[] peers = peers_raw.split(" ");
      this.handleRequestedPeers(peers, new_peer_hash, reply_channel);
      System.out.println("Sent requested nodes");
    }
  }

  private void handleSenderPeer(String sender_id, long sender_hash, PacketChannel reply_channel) {
    TableEntry curr;

    if ((curr = this.node.getResponsiblePeer(sender_hash)) != null) { // Peer in my range
      long          curr_hash     = curr.getResponsibleHash();
      PacketChannel curr_channel  = curr.getChannel();
      String        curr_hash_str = Long.toString(curr_hash);
      System.out.println("CURR = '" + curr_hash + "', MY = " + this.node.getHash());
      if (curr_hash == this.node.getHash()) { // Im the one responsible for this peer
        this.node.addPeer(sender_id, sender_hash, reply_channel);
      }
      else {
        if (curr_hash < sender_hash) { // Current is responsible for the peer
          curr_channel.sendPacket(Packet.newNewPeerPacket(sender_id, Long.toString(sender_hash), null));
        }
        else if (curr_hash > sender_hash) { //New is now responsible for current
          reply_channel.sendPacket(Packet.newPeerPacket(Long.toString(sender_hash), curr.getID()));
          curr.updateInfos(sender_id, sender_hash, reply_channel);
        }
        else {
          System.out.println("Damn that's nearly impossible!");
          System.exit(666);
        }
      }
    }
    else {
      TableEntry last_entry = this.node.lastEntry();
      if (last_entry.getResponsibleHash() == this.node.getHash()) { //New is now a sucessor of current
        reply_channel.sendPacket(Packet.newPeerPacket(Long.toString(sender_hash), sender_id));
      }
      else {
        PacketChannel resp_channel = last_entry.getChannel();
        Handler       handler      = new PeerHandler(this.node, reply_channel, sender_hash);
        PacketDispatcher.registerHandler(Packet.PEER, sender_hash, handler);
        resp_channel.sendPacket(Packet.newGetPeerPacket(Long.toString(sender_hash)));
      }
    }
  }

  private void handleRequestedPeers(String[] peers, long new_peer_hash, PacketChannel reply_channel) {
    long my_hash = this.node.getHash();

    for (String hash_str : peers) {
      long       peer_hash   = Long.parseLong(hash_str);
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
          SSLSocketListener.waitForWrite(reply_channel);
          if (!reply_channel.sendPacket(Packet.newPeerPacket(hash_str, this.node.getID()))) {
            System.err.println("  Failed to send '" + hash_str + "' to peer!");
          }
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
}
