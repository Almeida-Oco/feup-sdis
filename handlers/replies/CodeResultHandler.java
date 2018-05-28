package handlers.replies;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketChannel;

public class CodeResultHandler extends Handler {
  public CodeResultHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel buffer) {
    System.out.println(" --- Computation '" + packet.getHash() + "' result --- ");
    System.out.println(packet.getCode());
  }
}
