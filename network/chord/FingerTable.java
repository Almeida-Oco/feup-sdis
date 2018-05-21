import java.util.Vector;

class FingerTable {
  private static final int BIT_NUMBER = 64;
  private static final long MAX_ID    = (long)Math.pow(2, 64);
  long base_id;

  String predecessor;
  Vector<TableEntry> fingers;

  FingerTable(long base_id, String predecessor) {
    this.base_id     = base_id;
    this.predecessor = predecessor;
    this.fingers     = new Vector<TableEntry>(BIT_NUMBER);

    for (int i = 0, len = fingers.size(); i < len; i++) {
      long entry_id = Math.abs((base_id + (long)Math.pow(2, i)) % MAX_ID);
      this.fingers.set(i, new TableEntry(entry_id, null));
    }
  }

  public static int idToEntry(long id) {
    return (int)Math.ceil(Math.log(id) / Math.log(2));
  }

  void addPeer(String peer_ip, long peer_id) {
    int entry = FingerTable.idToEntry(peer_id);

    if (entry < this.base_id) {
      System.out.println("Send new to predecessor");
    }
    else {
      String current = this.fingers.get(entry).getNodeIP();
      for (int i = entry; i < BIT_NUMBER && this.fingers.get(i).getNodeIP() == current; i++) {
        this.fingers.get(i).updateNodeIP(peer_ip);
      }
    }
  }

  TableEntry getEntry(int entry_n) {
    if (entry_n >= BIT_NUMBER) {
      System.out.println("Send query to latest entry");
      return null;
    }
    else {
      return this.fingers.get(entry_n);
    }
  }
}

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
