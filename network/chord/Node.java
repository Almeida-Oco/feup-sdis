package network.chord;

import java.util.Random;
import java.util.Vector;
import java.util.Scanner;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import handlers.Handler;
import handlers.replies.*;
import network.comms.Packet;
import handlers.PacketDispatcher;
import network.comms.PacketChannel;
import network.comms.SSLSocketListener;

public class Node {
  public static final int BIT_NUMBER = 32;

  String my_id;
  long my_hash;
  FingerTable f_table;
  TableEntry predecessor;

  public Node(PacketChannel channel, int my_port) {
    try {
      this.my_id = InetAddress.getLocalHost().getHostAddress() + ":" + my_port;
    }
    catch (UnknownHostException err) {
      System.err.println("Host not known!\n - " + err.getMessage());
      System.exit(1);
    }

    this.my_hash = Node.hash(this.my_id.getBytes());
    this.f_table = new FingerTable(this.my_id, this.my_hash);

    this.predecessor = null;
    if (channel != null) {
      this.predecessor = new TableEntry(channel.getID(), 0, Node.hash(channel.getID().getBytes()), channel);
    }
  }

  /**
   * Initializes the peer discovery mechanism
   * @return The hash of the peers to be discovered
   */
  public boolean startNodeDiscovery() {
    Vector<String> params = new Vector<String>(2);
    params.add(Long.toString(this.my_hash));
    params.add(this.my_id);
    Vector<String> peers = new Vector<String>(32);

    for (int i = 0; i < BIT_NUMBER; i++) { //Need to discover who 'owns' my hash
      long peer_hash = (long)((my_hash + Math.pow(2, i)) % FingerTable.MAX_ID);
      peers.add(Long.toString(peer_hash));

      Handler handler = new PeerHandler(this, null, peer_hash);
      if (!PacketDispatcher.registerHandler(Packet.PEER, peer_hash, handler)) {
        return false;
      }
    }

    Packet packet = Packet.newNewPeerPacket(Long.toString(this.my_hash), this.my_id, peers);
    return this.predecessor.getChannel().sendPacket(packet);
  }

  public TableEntry getResponsiblePeer(long hash) {
    if (this.inMyRange(hash)) {
      return this.f_table.getEntry(hash);
    }
    else {
      return null;
    }
  }

  /**
   * Checks if the peer is in range
   * @param  peer_hash Peer to check
   * @return           Whether it is in range or not
   */
  public boolean inMyRange(long peer_hash) {
    long half = (long)Math.pow(2, BIT_NUMBER - 1);
    long last = (this.my_hash + half) % (long)Math.pow(2, BIT_NUMBER);

    if (peer_hash >= this.my_hash && peer_hash <= last) {
      return true;
    }
    else if (this.my_hash > half) {
      if (peer_hash >= this.my_hash && peer_hash >= last) {
        return true;
      }
      else if (peer_hash <= this.my_hash && peer_hash <= last) {
        return true;
      }
    }
    return false;
  }

  public void addPeer(String peer_id, long peer_hash, PacketChannel connection) {
    this.f_table.takeOverResponsibility(peer_id, peer_hash, connection);
  }

  public static long hash(byte[] content) {
    MessageDigest intestine;

    System.out.print("Hashing '" + new String(content) + "' --> ");
    try {
      intestine = MessageDigest.getInstance("SHA-1");
    }
    catch (NoSuchAlgorithmException err) {
      System.err.println(err.getMessage());
      return -1;
    }

    byte[]     cont = intestine.digest(content);
    LongBuffer lb   = ByteBuffer.wrap(cont).order(ByteOrder.BIG_ENDIAN).asLongBuffer();

    long hash = Math.abs(lb.get(1)) % (long)Math.pow(2, BIT_NUMBER);
    System.out.println(hash);
    return hash;
  }

  public String getID() {
    return this.my_id;
  }

  public long getHash() {
    return this.my_hash;
  }

  public TableEntry getPredecessor() {
    return this.predecessor;
  }

  public void setPredecessor(String peer_id, long peer_hash, PacketChannel channel) {
    this.predecessor.updateInfos(peer_id, peer_hash, channel);
  }

  public void setPredecessor(PacketChannel channel) {
    String id   = channel.getID();
    long   hash = Node.hash(id.getBytes());

    this.predecessor.updateInfos(id, hash, channel);
  }

  public void setAlive(String peer_id, long entry_hash) {
    Vector<TableEntry> entries = this.f_table.getEntries();

    if (entry_hash == this.predecessor.getResponsibleHash()) {
      System.out.println("reviving predecessor");
      this.predecessor.revive();
    }

    for (TableEntry entry : entries) {
      System.out.print("ID = '" + entry.getID() + "' == '" + peer_id + "'");
      if (entry.getID().equals(peer_id)) {
        System.out.println("  true");
        entry.revive();
      }
    }
  }

  public void killAllEntries() {
    if (this.predecessor != null) {
      this.predecessor.kill();
    }

    for (TableEntry entry : this.f_table.getEntries()) {
      if (entry.getResponsibleHash() != this.my_hash) {
        entry.kill();
      }
    }
  }

  public TableEntry lastEntry() {
    return this.f_table.getLastEntry();
  }

  public long maxHash() {
    return this.f_table.maxHash();
  }

  public Vector<TableEntry> getAllEntries() {
    Vector<TableEntry> entry_channels = new Vector<TableEntry>(BIT_NUMBER + 1);
    if (this.predecessor != null) {
      entry_channels.add(this.predecessor);
    }

    for (TableEntry entry : this.f_table.getEntries()) {
      entry_channels.add(entry);
    }

    return entry_channels;
  }

  public Vector<TableEntry> getDeadEntries() {
    Vector<TableEntry> entry_channels = new Vector<TableEntry>(BIT_NUMBER + 1);

    if (this.predecessor != null&& !this.predecessor.isAlive()) {
      entry_channels.add(this.predecessor);
    }

    for (TableEntry entry : this.f_table.getEntries()) {
      if (!entry.isAlive()) {
        entry_channels.add(entry);
      }
    }

    return entry_channels;
  }

  @Override
  public String toString() {
    String ret;

    if (this.predecessor != null) {
      ret = "FATHER = " + this.predecessor.getID() + "\n";
    }
    else {
      ret = "FATHER = MYSELF\n";
    }

    return ret + this.f_table.toString();
  }
}
