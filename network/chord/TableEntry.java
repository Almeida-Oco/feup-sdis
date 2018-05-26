package network.chord;

import network.comms.PacketBuffer;

class TableEntry {
  long entry_id;
  String node_id;
  PacketBuffer node_channel;

  TableEntry(long entry_id, String node_id) {
    this.entry_id = entry_id;
    this.node_id  = node_id;
  }

  long getNodeID() {
    return this.entry_id;
  }

  String getNodeIP() {
    return this.node_id;
  }

  PacketBuffer getChannel() {
    return this.node_channel;
  }

  void updateNodeIP(String node_id) {
    this.node_id = node_id;
  }

  @Override
  public String toString() {
    return "(" + entry_id + ", " + node_id + ")";
  }
}
