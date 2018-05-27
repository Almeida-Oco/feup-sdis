package handlers.queries;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketBuffer;

public class GetPeerHandler extends Handler {
  public GetPeerHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketBuffer buffer) {
  }

  @Override
  public void run() {
  }
}
