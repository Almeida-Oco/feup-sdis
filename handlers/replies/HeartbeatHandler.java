package handlers.replies;

import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.chord.TableEntry;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import network.comms.SSLSocketListener;

public class HeartbeatHandler extends Handler {
  TableEntry entry;

  public HeartbeatHandler(Node node, TableEntry entry) {
    super(node);
    this.entry = entry;
  }

  @Override
  public void run(Packet packet, PacketChannel reply_channel) {
    SSLSocketListener.unregisterChannel(reply_channel);
    PacketDispatcher.unregisterHandler(packet.getType(), packet.getHash());
    this.entry.revive();
  }
}
