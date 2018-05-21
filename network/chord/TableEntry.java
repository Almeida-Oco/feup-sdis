package network.chord;

class TableEntry {
  long entry_id;
  String node_ip;

  TableEntry(long entry_id, String node_ip) {
    this.entry_id = entry_id;

    this.node_ip = node_ip;
  }

  long getNodeID() {
    return this.entry_id;
  }

  String getNodeIP() {
    return this.node_ip;
  }

  void updateNodeIP(String node_ip) {
    this.node_ip = node_ip;
  }
}
