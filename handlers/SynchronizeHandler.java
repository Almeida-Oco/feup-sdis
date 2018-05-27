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
    System.out.println("Awoken!");
    System.out.println(this.node.toString());
    Vector<TableEntry> dead_entries = this.node.getDeadEntries();
    if (dead_entries.size() > 0) {
      System.out.println("There are dead entries to prune!");
    }

    this.node.killAllEntries();
    Vector<TableEntry> entries = this.node.getAllEntries();
    Packet             packet  = Packet.newAlivePacket();

    for (TableEntry entry : entries) {
      PacketChannel channel = entry.getChannel();
      if (channel != null) {
        SSLSocketListener.waitForRead(channel);
        PacketDispatcher.registerHandler(Packet.HEARTBEAT, entry.getHash(), new HeartbeatHandler(this.node));
        channel.sendPacket(packet);
      }
    }
    System.out.println("--- Ended synchronize!!");
  }
}
