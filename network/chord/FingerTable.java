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

  Vector<TableEntry> fingers = new Vector<TableEntry>(BIT_NUMBER);

  public static int hashToEntry(long id) {
    return (int)Math.floor(Math.log(id) / Math.log(2)) % BIT_NUMBER;
  }

  FingerTable(String my_id, long my_hash) {
    this.my_hash         = my_hash;
    this.max_finger_hash = Math.abs((my_hash + (long)Math.pow(2, BIT_NUMBER - 1)) % MAX_ID);
    this.fingers.ensureCapacity(BIT_NUMBER);

    for (int i = 0; i < BIT_NUMBER; i++) {
      long entry_hash = Math.abs((my_hash + (long)Math.pow(2, i)) % MAX_ID);
      this.fingers.add(new TableEntry(my_id, entry_hash, null));
    }
  }

  boolean shouldTakeOver(long new_peer_hash) {
    int  index     = FingerTable.hashToEntry(new_peer_hash);
    long curr_hash = this.fingers.get(index).getHash();

    return new_peer_hash < curr_hash;
  }

  public void takeOverResponsibility(long new_hash, TableEntry new_entry) {
    int  index     = FingerTable.hashToEntry(new_hash);
    long curr_hash = this.fingers.get(index).getHash();

    for (int i = index; i < BIT_NUMBER; i++) {
      if (this.fingers.get(i).getHash() != curr_hash) {
        break;
      }
      this.fingers.set(i, new_entry);
    }
  }

  TableEntry getEntry(long peer_hash) {
    int entry_index = FingerTable.hashToEntry(peer_hash);

    if (peer_hash >= this.max_finger_hash) {
      return this.fingers.lastElement();
    }
    return this.fingers.get(entry_index);
  }

  TableEntry getLastEntry() {
    return this.fingers.lastElement();
  }

  public long maxHash() {
    return this.max_finger_hash;
  }

  Vector<TableEntry> getEntries() {
    return this.fingers;
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
