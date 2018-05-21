import java.net.InetAddress;

class Node {
  String predecessor;
  FingerTable fingers;

  private static long hash(byte[] content) {
    long h = 1125899906842597L;

    for (byte bt : content) {
      h = 31 * h + bt;
    }

    return h;
  }

  public Node(String remote_ip, int port) {
    this.predecessor = null;
    this.fingers     = new FingerTable(Node.hash(remote_ip.getBytes()), null);
  }

  public void sendCode(String code) {
    int        entry_index = FingerTable.idToEntry(Node.hash(code.getBytes()));
    TableEntry entry       = this.fingers.getEntry(entry_index);

    System.out.println("IP = '" + entry.getNodeIP() + "'");
  }
}
