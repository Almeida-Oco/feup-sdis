package network.chord;

import java.util.Scanner;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Node {
  private static final int BIT_NUMBER = 64;

  String predecessor;
  FingerTable fingers;

  public static void main(String[] args) {
    Node node = new Node("127.0.0.1", 8080);

    String[] ips = { "192.168.1.1", "162.742.1.2", "192.172.1.3", "115.168.1.4", "112.264.197.222" };

    for (String ip : ips) {
      node.fingers.addPeer(ip, Node.hash(ip.getBytes()));
    }

    Scanner in = new Scanner(System.in);
    String  line;

    while (!(line = in.nextLine()).equals("stop")) {
      node.sendCode(line);
    }
  }

  private static long hash(byte[] content) {
    MessageDigest intestine;

    try {
      intestine = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException err) {
      System.err.println(err.getMessage());
      return -1;
    }

    byte[] cont = intestine.digest(content);
    System.out.println("SIZE = " + cont.length);
    LongBuffer lb = ByteBuffer.wrap(intestine.digest(content), BIT_NUMBER, BIT_NUMBER).order(ByteOrder.BIG_ENDIAN).asLongBuffer();

    return lb.get();
  }

  public Node(String remote_ip, int port) {
    this.predecessor = null;

    this.fingers = new FingerTable(Node.hash(remote_ip.getBytes()), null);
  }

  public void sendCode(String code) {
    int        entry_index = FingerTable.idToEntry(Node.hash(code.getBytes()));
    TableEntry entry       = this.fingers.getEntry(entry_index);

    System.out.println("IP = '" + entry.getNodeIP() + "'");
  }
}
