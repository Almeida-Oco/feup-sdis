package network.chord;

import java.util.Vector;
import java.util.LinkedHashMap;

class FingerTable {
  private static final int BIT_NUMBER = Node.BIT_NUMBER;
  public static final long MAX_ID     = (long)Math.pow(2, BIT_NUMBER);
  long base_id;
  long max_finger_id;

  String predecessor;
  Vector<TableEntry> fingers = new Vector<TableEntry>(BIT_NUMBER);

  FingerTable(long base_id, String predecessor) {
    this.base_id       = base_id;
    this.max_finger_id = Math.abs((base_id + (long)Math.pow(2, BIT_NUMBER - 1)) % MAX_ID);
    this.predecessor   = predecessor;
    this.fingers.ensureCapacity(BIT_NUMBER);

    for (int i = 0; i < BIT_NUMBER; i++) {
      long entry_id = Math.abs((base_id + (long)Math.pow(2, i)) % MAX_ID);
      this.fingers.add(new TableEntry(entry_id, null));
    }
  }

  public static int idToEntry(long id) {
    return (int)Math.ceil(Math.log(id) / Math.log(2)) % BIT_NUMBER;
  }

  void addPeer(String peer_ip, long peer_id, LinkedHashMap<String, Node> nodes) {
    int entry = FingerTable.idToEntry(peer_id);

    if (peer_id > (long)(this.base_id + Math.pow(2, BIT_NUMBER - 1))) {
      System.out.println(" ---- Send new to predecessor ----- ");
    }
    else {
      String current = this.fingers.get(entry).getNodeIP();
      if (current != null) {
        nodes.get(current).addSucessor(peer_ip, peer_id, nodes);
      }
      else {
        for (int i = entry; i < BIT_NUMBER && this.fingers.get(i).getNodeIP() == current; i++) {
          this.fingers.get(i).updateNodeIP(peer_ip);
        }
      }
    }
  }

  void setPredecessor(String peer_ip) {
    this.predecessor = peer_ip;
  }

  TableEntry getEntry(long peer_id) {
    int entry_index = FingerTable.idToEntry(peer_id);

    if (entry_index >= BIT_NUMBER) {
      System.out.println("Send to latest sucessor");
      return null;
    }
    else {
      return this.fingers.get(entry_index);
    }
  }

  @Override
  public String toString() {
    String final_str = "[";

    for (int i = 0; i < fingers.size(); i++) {
      final_str += i + " - " + fingers.get(i).toString() + ",\n";
    }

    return final_str;
  }
}
