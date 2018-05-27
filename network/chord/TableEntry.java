package network.chord;

import network.comms.PacketBuffer;

class TableEntry {
  long entry_hash;
  String entry_id;
  PacketBuffer node_channel;

  TableEntry(String entry_id, long entry_hash, PacketBuffer connection) {
    this.entry_hash   = entry_hash;
    this.entry_id     = entry_id;
    this.node_channel = connection;
  }

  long getEntryID() {
    return this.entry_hash;
  }

  String getNodeID() {
    return this.entry_id;
  }

  PacketBuffer getChannel() {
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
