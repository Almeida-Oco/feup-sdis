package handlers.replies;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketBuffer;

public class PeerHandler extends Handler {
  public PeerHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketBuffer buffer) {
  }

  @Override
  public void run() {
  }
}
