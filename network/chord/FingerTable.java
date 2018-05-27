package network.chord;

import java.util.Vector;
import java.util.LinkedHashMap;

import network.comms.Packet;
import network.chord.TableEntry;
import network.comms.PacketChannel;

class FingerTable {
  private static final int BIT_NUMBER = Node.BIT_NUMBER;
  public static final long MAX_ID     = (long)Math.pow(2, BIT_NUMBER);
  long my_hash;
  long max_finger_hash;

  TableEntry predecessor;
  Vector<TableEntry> fingers = new Vector<TableEntry>(BIT_NUMBER);

  public static int hashToEntry(long id) {
    return (int)Math.ceil(Math.log(id) / Math.log(2)) % BIT_NUMBER;
  }

  FingerTable(String my_id, long my_hash) {
    this.my_hash         = my_hash;
    this.max_finger_hash = Math.abs((my_hash + (long)Math.pow(2, BIT_NUMBER - 1)) % MAX_ID);
    this.predecessor     = null;
    this.fingers.ensureCapacity(BIT_NUMBER);

    for (int i = 0; i < BIT_NUMBER; i++) {
      long entry_hash = Math.abs((my_hash + (long)Math.pow(2, i)) % MAX_ID);
      this.fingers.add(new TableEntry(my_id, entry_hash, null));
    }
  }

  void addPeer(String peer_id, long peer_hash, PacketChannel connection) {
    int        index = FingerTable.hashToEntry(peer_hash);
    TableEntry entry = new TableEntry(peer_id, peer_hash, connection);

    long current = this.fingers.get(index).getHash();

    for (int i = index; i < BIT_NUMBER; i++) {
      if (this.fingers.get(i).getHash() != current) {
        break;
      }
      this.fingers.set(i, entry);
    }
  }

  void setPredecessor(String peer_id, long peer_hash, PacketChannel connection) {
    this.predecessor = new TableEntry(peer_id, peer_hash, connection);
  }

  TableEntry getEntry(long peer_hash) {
    int entry_index = FingerTable.hashToEntry(peer_hash);

    if (entry_index >= BIT_NUMBER) {
      return null;
    }
    return this.fingers.get(entry_index);
  }

  TableEntry getLastEntry() {
    return this.fingers.lastElement();
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
