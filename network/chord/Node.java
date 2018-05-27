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

import network.comms.Packet;
import network.comms.PacketBuffer;
import network.comms.SSLSocketListener;
import network.comms.sockets.SSLSocketChannel;

public class Node {
  public static final int BIT_NUMBER = 32;

  String my_id;
  long my_hash;
  FingerTable f_table;
  PacketBuffer start_buffer;

  public Node(SSLSocketChannel channel, InetSocketAddress myself) {
    this.my_id        = Node.getID(myself.getPort());
    this.start_buffer = new PacketBuffer(channel);
    this.my_hash      = Node.hash(this.my_id.getBytes());
    this.f_table      = new FingerTable(this.my_id, this.my_hash);
  }

  /**
   * Initializes the peer discovery mechanism
   * @return The hash of the peers to be discovered
   */
  public Vector<String> startNodeDiscovery() {
    Vector<String> params = new Vector<String>(2);
    params.add(Long.toString(this.my_hash));
    params.add(this.my_id);
    Vector<String> peers = new Vector<String>(32);

    for (int i = 0; i <= BIT_NUMBER; i++) { //Need to discover who 'owns' my hash
      peers.add(Long.toString((long)(my_hash + Math.pow(2, i)) % FingerTable.MAX_ID));
    }

    Packet packet = Packet.newNewPeerPacket(Long.toString(this.my_hash), this.my_id, peers);
    this.start_buffer.sendPacket(packet);
    return peers;
  }

  public void findRequestedNodes(Packet packet, PacketBuffer buffer) {
    String[] nodes_hash = packet.getCode().split(" ");
    for (String hash : nodes_hash) {
      TableEntry entry = this.f_table.getEntry(Long.parseLong(hash));
      if (entry != null) {
        buffer.sendPacket(Packet.newPeerPacket(hash, entry.getNodeID()));
      }
      else { //Request closes node to find it (register somewhere that I am looking for peer with this hash)
      }
    }
  }

  public void setPredecessor(String peer_ip, long peer_hash, PacketBuffer connection) {
    this.f_table.setPredecessor(peer_ip, peer_hash, connection);
  }

  public void addSucessor(String peer_id, long peer_hash, PacketBuffer connection) {
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

    System.out.println("IP = '" + entry.getNodeID() + "'");
  }

  private static String getID(int port) {
    try {
      return InetAddress.getLocalHost().getHostAddress() + ":" + port;
    }
    catch (UnknownHostException err) {
      System.err.println("Host not known");
      return null;
    }
  }
}
