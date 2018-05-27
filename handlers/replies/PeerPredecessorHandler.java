package handlers.replies;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketChannel;

public class PeerPredecessorHandler extends Handler {
  public PeerPredecessorHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel buffer) {
  }

  @Override
  public void run() {
  }
}
