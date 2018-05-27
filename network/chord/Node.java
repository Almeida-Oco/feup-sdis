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
import network.comms.sockets.SSLSocketChannel;

public class Node {
  public static final int BIT_NUMBER = 32;

  String my_id;
  long my_hash;
  FingerTable f_table;
  PacketChannel start_buffer;

  public Node(SSLSocketChannel channel, int my_port) {
    try {
      this.my_id = InetAddress.getLocalHost().getHostAddress() + ":" + my_port;
    }
    catch (UnknownHostException err) {
      System.err.println("Host not known!\n - " + err.getMessage());
      System.exit(1);
    }
    this.start_buffer = new PacketChannel(channel);
    this.my_hash      = Node.hash(this.my_id.getBytes());
    this.f_table      = new FingerTable(this.my_id, this.my_hash);
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

    for (int i = 0; i <= BIT_NUMBER; i++) { //Need to discover who 'owns' my hash
      long peer_hash = (long)((my_hash + Math.pow(2, i)) % FingerTable.MAX_ID);
      peers.add(Long.toString(peer_hash));

      Handler handler = new PeerHandler(this, null, peer_hash);
      if (!PacketDispatcher.registerHandler(Packet.PEER, peer_hash, handler)) {
        System.err.println("Hash '" + peer_hash + "' already present?!");
        return false;
      }
    }
    Packet packet = Packet.newNewPeerPacket(Long.toString(this.my_hash), this.my_id, peers);
    System.out.println("Sending NEW_PEER");
    return this.start_buffer.sendPacket(packet);
  }

  public void findRequestedNodes(Packet packet, PacketChannel buffer) {
    String[] nodes_hash = packet.getCode().split(" ");
    for (String hash : nodes_hash) {
      TableEntry entry = this.f_table.getEntry(Long.parseLong(hash));
      if (entry != null) {
        buffer.sendPacket(Packet.newPeerPacket(hash, entry.getID()));
      }
      else { //Request closes node to find it (register somewhere that I am looking for peer with this hash)
        TableEntry    last_entry  = this.f_table.getLastEntry();
        PacketChannel peer_buffer = last_entry.getChannel();
      }
    }
  }

  public TableEntry getResponsiblePeer(long hash) {
    TableEntry entry = this.f_table.getEntry(hash);

    if (entry != null) {
      return entry;
    }
    return this.f_table.getLastEntry();
  }

  public void setPredecessor(String peer_ip, long peer_hash, PacketChannel connection) {
    this.f_table.setPredecessor(peer_ip, peer_hash, connection);
  }

  public void addPeer(String peer_id, long peer_hash, PacketChannel connection) {
    this.f_table.addPeer(peer_id, peer_hash, connection);
  }

  static long hash(byte[] content) {
    MessageDigest intestine;

    try {
      intestine = MessageDigest.getInstance("SHA-1");
    }
    catch (NoSuchAlgorithmException err) {
      System.err.println(err.getMessage());
      return -1;
    }

    byte[]     cont = intestine.digest(content);
    LongBuffer lb   = ByteBuffer.wrap(cont).order(ByteOrder.BIG_ENDIAN).asLongBuffer();

    return Math.abs(lb.get(1)) % (long)Math.pow(2, BIT_NUMBER);
  }

  public void sendCode(String code) {
    int        entry_index = FingerTable.hashToEntry(Node.hash(code.getBytes()));
    TableEntry entry       = this.f_table.getEntry(entry_index);

    System.out.println("IP = '" + entry.getID() + "'");
  }

  public String getID() {
    return this.my_id;
  }

  public long getHash() {
    return this.my_hash;
  }
}
