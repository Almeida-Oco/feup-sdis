package handlers.queries;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.chord.TableEntry;
import network.comms.PacketChannel;

public class PredecessorHandler extends Handler {
  public PredecessorHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel reply_channel) {
    TableEntry predecessor = this.node.getPredecessor();
    long       sender_hash = packet.getHash();

    if (sender_hash == this.node.getHash()) { // Telling me who my predecessor is
      this.node.setPredecessor(reply_channel.getID(), sender_hash, reply_channel);
    }
    else {
      if (predecessor != null) {
        String pred_hash = Long.toString(predecessor.getResponsibleHash());
        String pred_id   = predecessor.getID();

        reply_channel.sendPacket(Packet.newFatherPacket(pred_hash, pred_id));
      }
      else {
        System.err.println("I have no predecessor defined?!");
      }
    }
  }
}
