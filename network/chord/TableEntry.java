package network.chord;

import network.comms.PacketChannel;

public class TableEntry {
  long entry_hash;
  long responsible_hash;
  String entry_id;
  PacketChannel node_channel;
  boolean alive;

  TableEntry(String entry_id, long entry_hash, long resp_hash, PacketChannel connection) {
    this.entry_hash       = entry_hash;
    this.responsible_hash = resp_hash;
    this.entry_id         = entry_id;
    this.node_channel     = connection;
    this.alive            = true;
  }

  public synchronized long getEntryHash() {
    return this.entry_hash;
  }

  public synchronized long getResponsibleHash() {
    return this.responsible_hash;
  }

  public synchronized String getID() {
    return this.entry_id;
  }

  public synchronized PacketChannel getChannel() {
    return this.node_channel;
  }

  synchronized boolean isAlive() {
    synchronized (this) {
      return this.alive;
    }
  }

  public synchronized void updateInfos(String id, long hash, PacketChannel channel) {
    this.entry_id         = id;
    this.responsible_hash = hash;
    this.node_channel     = channel;
  }

  public synchronized void revive() {
    this.alive = true;
  }

  synchronized void kill() {
    this.alive = false;
  }

  @Override
  public String toString() {
    return "(" + entry_hash + ", " + entry_id + ", " + alive + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TableEntry) {
      TableEntry other = (TableEntry)obj;
      return other.responsible_hash == this.responsible_hash;
    }
    return false;
  }
}
