package handlers;

import java.util.Vector;

import network.chord.Node;
import network.comms.Packet;
import network.chord.TableEntry;
import network.comms.PacketChannel;
import handlers.replies.HeartbeatHandler;
import network.comms.SSLSocketListener;

public class SynchronizeHandler extends Handler {
  public SynchronizeHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel buffer) {
  }

  @Override
  public void run() {
    Vector<TableEntry> dead_entries = this.node.getDeadEntries();
    System.out.println(this.node.toString());
    if (dead_entries.size() > 0) {
      System.out.println("  There are faults in network!");
      //TODO handle network failures
    }

    this.node.killAllSuccessors();
    Vector<TableEntry> entries = this.node.getAllSucessors();
    Packet             packet  = Packet.newAlivePacket();

    for (TableEntry entry : entries) {
      PacketChannel channel = entry.getChannel();
      if (channel != null) {
        SSLSocketListener.waitForRead(channel);
        PacketDispatcher.registerHandler(Packet.HEARTBEAT, entry.getResponsibleHash(), new HeartbeatHandler(this.node, entry));
        channel.sendPacket(packet);
      }
    }
  }
}
