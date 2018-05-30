package handlers.replies;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketChannel;
import network.comms.SSLSocketListener;

public class CodeResultHandler extends Handler {
  public CodeResultHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel buffer) {
    System.out.println("\n --- Computation '" + packet.getHash() + "' result ---");
    System.out.println(packet.getCode());
    System.out.println("\n --- END of RESULT --- \n ");
    SSLSocketListener.shutdown();
  }
}
