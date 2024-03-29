package handlers.queries;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketChannel;

public class DropoutHandler extends Handler {
  public DropoutHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel buffer) {
    System.out.println("Peer '" + packet.getHash() + "' is leaving!");
  }
}
