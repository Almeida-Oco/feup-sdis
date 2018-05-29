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
    long       peer_hash   = packet.getHash();
    String     hash_str    = Long.toString(peer_hash);
    TableEntry responsible = this.node.getResponsiblePeer(peer_hash);

    if (responsible != null) { // peer in my range
      PacketChannel responsible_channel = responsible.getChannel();

      if (responsible_channel != null) { // Someone else successor
        System.out.println("Resp ID = " + responsible.getID() + ", repli ID = " + reply_channel.getID());
        Handler handler = new PeerHandler(this.node, reply_channel, peer_hash);
        PacketDispatcher.registerHandler(Packet.PEER, peer_hash, handler);
        responsible_channel.sendPacket(Packet.newGetPeerPacket(hash_str));
      }
      else {
        System.out.println("My successor '" + hash_str + "'");
        reply_channel.sendPacket(Packet.newPeerPacket(hash_str, this.node.getID()));
      }
    }
    else {
      TableEntry last_entry = this.node.lastEntry();
      if (last_entry.getResponsibleHash() != this.node.getHash()) { //Last entry might know
        if (last_entry.getID() != reply_channel.getID()) {
          Handler handler = new PeerHandler(this.node, reply_channel, peer_hash);
          PacketDispatcher.registerHandler(Packet.PEER, peer_hash, handler);
          System.out.println("Last = " + last_entry.getID() + ", reply ID = " + reply_channel.getID());
          last_entry.getChannel().sendPacket(Packet.newGetPeerPacket(hash_str));
        }
      }
    }
  }

  public static void handlePeer(Node node, String peer_id, long peer_hash, PacketChannel reply_channel) {
    long       my_hash = node.getHash();
    TableEntry responsible;

    if ((responsible = node.getResponsiblePeer(peer_hash)) != null) {
      PacketChannel responsible_channel = responsible.getChannel();

      if (responsible_channel != null) {
        if (responsible.getID() != peer_id) {
          Handler handler = new PeerHandler(node, reply_channel, peer_hash);
          PacketDispatcher.registerHandler(Packet.PEER, peer_hash, handler);
          responsible_channel.sendPacket(Packet.newGetPeerPacket(Long.toString(peer_hash)));
        }
        else {
          System.out.println("My successor '" + peer_hash + "'");
          SSLSocketListener.waitForWrite(reply_channel);
          reply_channel.sendPacket(Packet.newPeerPacket(Long.toString(peer_hash), node.getID()));
        }
      }
    }
    else {
      TableEntry    last_entry   = node.lastEntry();
      PacketChannel last_channel = last_entry.getChannel();
      if (last_entry.getID() != peer_id) {
        System.out.println("Checking last entry");
        Handler handler = new PeerHandler(node, reply_channel, peer_hash);
        PacketDispatcher.registerHandler(Packet.PEER, peer_hash, handler);

        SSLSocketListener.waitForWrite(last_channel);
        last_channel.sendPacket(Packet.newGetPeerPacket(Long.toString(peer_hash)));
      }
    }
  }

  public static void redirectTo(TableEntry entry, Node node, String peer_id, long peer_hash, PacketChannel redirect_channel) {
    if (entry.getID() != peer_id && entry.getResponsibleHash() != peer_hash) {
      System.out.println("Redirecting to: " + entry.toString() + "\n GOT: hash '" + peer_hash + "', id '" + peer_id + "'");
      Handler handler = new PeerHandler(node, redirect_channel, peer_hash);
      PacketDispatcher.registerHandler(Packet.PEER, peer_hash, handler);
      entry.getChannel().sendPacket(Packet.newGetPeerPacket(Long.toString(peer_hash)));
    }
  }
}
