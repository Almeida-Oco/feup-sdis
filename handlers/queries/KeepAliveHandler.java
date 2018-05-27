package handlers.queries;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketChannel;

public class KeepAliveHandler extends Handler {
  public KeepAliveHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel buffer) {
    buffer.sendPacket(Packet.newHeartbeatPacket(Long.toString(this.node.getHash())));
  }

  @Override
  public void run() {
  }
}
