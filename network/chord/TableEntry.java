package network.chord;

import network.comms.PacketChannel;

public class TableEntry {
  long entry_hash;
  String entry_id;
  PacketChannel node_channel;
  boolean alive;

  TableEntry(String entry_id, long entry_hash, PacketChannel connection) {
    this.entry_hash   = entry_hash;
    this.entry_id     = entry_id;
    this.node_channel = connection;
    this.alive        = true;
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

  boolean isAlive() {
    return this.alive;
  }

  void updateNodeIP(String entry_id) {
    this.entry_id = entry_id;
  }

  void revive() {
    this.alive = true;
  }

  void kill() {
    this.alive = false;
  }

  @Override
  public String toString() {
    return "(" + entry_hash + ", " + entry_id + ")";
  }
}
