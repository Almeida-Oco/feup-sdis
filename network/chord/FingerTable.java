package network.chord;

import java.util.Vector;

class FingerTable {
  private static final int BIT_NUMBER = 64;
  private static final long MAX_ID    = (long)Math.pow(2, 64);
  long base_id;

  String predecessor;
  Vector<TableEntry> fingers = new Vector<TableEntry>(BIT_NUMBER);

  FingerTable(long base_id, String predecessor) {
    this.base_id     = base_id;
    this.predecessor = predecessor;
    this.fingers.ensureCapacity(BIT_NUMBER);

    for (int i = 0; i < BIT_NUMBER; i++) {
      long entry_id = Math.abs((base_id + (long)Math.pow(2, i)) % MAX_ID);
      this.fingers.add(new TableEntry(entry_id, null));
    }
  }

  public static int idToEntry(long id) {
    return (int)Math.ceil(Math.log(id) / Math.log(2));
  }

  void addPeer(String peer_ip, long peer_id) {
    int entry = FingerTable.idToEntry(peer_id);

    if (Math.abs(entry - this.base_id) > Math.pow(2, BIT_NUMBER - 1)) {
      System.out.println("Send new to predecessor");
    }
    else {
      System.out.println("Added ID " + peer_id + ", IP = '" + peer_ip + "'");
      String current = this.fingers.get(entry).getNodeIP();
      for (int i = entry; i < BIT_NUMBER && this.fingers.get(i).getNodeIP() == current; i++) {
        this.fingers.get(i).updateNodeIP(peer_ip);
      }
    }
  }

  TableEntry getEntry(int entry_n) {
    System.out.println("Querying entry_n = " + entry_n);
    if (entry_n >= BIT_NUMBER) {
      System.out.println("Send query to latest entry");
      return null;
    }
    else {
      return this.fingers.get(entry_n);
    }
  }
}
