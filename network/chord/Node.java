package network.chord;

import java.util.Random;
import java.util.Vector;
import java.util.Scanner;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.ByteBuffer;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Node {
  private static final int BIT_NUMBER = 32;

  long finger_id;
  String predecessor;
  FingerTable neighbors;

  public static void main(String[] args) {
    Node   node = new Node("127.0.0.1", 8080);
    Random rand = new Random();

    LinkedHashMap<String, Node> nodes = new LinkedHashMap<String, Node>(20);
    Vector<String> ips = new Vector<String>(20);

    for (int i = 0; i < 20; i++) {
      String ip = "" + rand.nextInt(256) + "." + rand.nextInt(256) + "." + rand.nextInt(256) + "." + rand.nextInt(256);
      ips.add(ip);
      nodes.putIfAbsent(ip, new Node(ip, 8080));
    }

    for (String ip : ips) {
      node.neighbors.addPeer(ip, Node.hash(ip.getBytes()), nodes);
    }
  }

  public void setPredecessor(String peer_ip) {
    this.neighbors.setPredecessor(peer_ip);
  }

  public void addSucessor(String peer_ip, long peer_id, LinkedHashMap<String, Node> nodes) {
    this.neighbors.addPeer(peer_ip, peer_id, nodes);
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

  public Node(String remote_ip, int port) {
    this.predecessor = null;

    this.neighbors = new FingerTable(Node.hash(remote_ip.getBytes()), null);
  }

  public void sendCode(String code) {
    int        entry_index = FingerTable.idToEntry(Node.hash(code.getBytes()));
    TableEntry entry       = this.neighbors.getEntry(entry_index);

    System.out.println("IP = '" + entry.getNodeIP() + "'");
  }
}
