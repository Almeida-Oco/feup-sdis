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

  String finger_ip;
  long finger_hash;
  FingerTable f_table;
  PacketBuffer start_buffer;
  SSLSocketListener listener;

  public Node(SSLSocketChannel channel, InetSocketAddress myself) {
    this.finger_ip    = Node.getID(myself.getPort());
    this.start_buffer = new PacketBuffer(channel);
    this.finger_hash  = Node.hash(this.finger_ip.getBytes());
    this.f_table      = new FingerTable(this.finger_hash, null);
  }

  public boolean startNodeDiscovery() {
    Vector<String> params = new Vector<String>(2);
    params.add(Long.toString(this.finger_hash));
    params.add(this.finger_ip);
    String peers = "";

    for (int i = 0; i < BIT_NUMBER; i++) {
      String peer = Long.toString((long)(finger_hash + Math.pow(2, i)) % FingerTable.MAX_ID);
      peers += peer + " ";
    }
    Packet packet = Packet.newPacket("NEW_PEER", params, peers.substring(0, peers.length() - 1));
    return this.start_buffer.sendPacket(packet);
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

  public void setListener(SSLSocketListener listener) {
    this.listener = listener;
  }
}
