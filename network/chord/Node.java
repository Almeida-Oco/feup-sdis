package network.chord;

import network.comms.sockets.SSLSocketChannel;

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
import java.security.NoSuchAlgorithmException;

public class Node {
  public static final int BIT_NUMBER = 32;

  long finger_id;
  SSLSocketChannel base_node;
  FingerTable f_table;

  public Node(SSLSocketChannel channel, InetSocketAddress myself) {
    this.base_node = channel;

    if (channel != null) {
      this.f_table = new FingerTable(Node.hash(channel.getID().getBytes()), null);
    }
    else {
      this.f_table = new FingerTable(Node.hash(Node.getID(myself).getBytes()), null);
    }
  }

  private static String getID(InetSocketAddress myself) {
    return myself.getHostString() + ":" + myself.getPort();
  }

  public void setPredecessor(String peer_ip) {
    this.f_table.setPredecessor(peer_ip);
  }

  public void addSucessor(String peer_ip, long peer_id, LinkedHashMap<String, Node> nodes) {
    this.f_table.addPeer(peer_ip, peer_id, nodes);
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
    int        entry_index = FingerTable.idToEntry(Node.hash(code.getBytes()));
    TableEntry entry       = this.f_table.getEntry(entry_index);

    System.out.println("IP = '" + entry.getNodeIP() + "'");
  }
}
