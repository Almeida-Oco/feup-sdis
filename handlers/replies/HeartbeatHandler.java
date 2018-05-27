package handlers.replies;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import network.comms.SSLSocketListener;

public class HeartbeatHandler extends Handler {
  public HeartbeatHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel buffer) {
    SSLSocketListener.unregisterChannel(buffer);
    PacketDispatcher.unregisterHandler(packet.getType(), packet.getHash());
    System.out.println("HEARTBEAT 1");
  }

  @Override
  public void run() {
    System.out.println("HEARTBEAT 2");
  }
}
