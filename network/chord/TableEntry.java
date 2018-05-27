package network.chord;

import network.comms.PacketChannel;

public class TableEntry {
  long entry_hash;
  String entry_id;
  PacketChannel node_channel;

  TableEntry(String entry_id, long entry_hash, PacketChannel connection) {
    this.entry_hash   = entry_hash;
    this.entry_id     = entry_id;
    this.node_channel = connection;
  }

  public long getHash() {
    return this.entry_hash;
  }

  public String getID() {
    return this.entry_id;
  }

  public PacketChannel getChannel() {
    return this.node_channel;
  }

  void updateNodeIP(String entry_id) {
    this.entry_id = entry_id;
  }

  @Override
  public String toString() {
    return "(" + entry_hash + ", " + entry_id + ")";
  }
}
