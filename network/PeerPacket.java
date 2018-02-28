package network;

public class PeerPacket {
  PacketInfo info;

  public static void main(String[] args) {
    String txt = "MDR 1.4 1234 AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA 4 9\r\n\r\nDATA\r\n";
    PacketInfo packet = PacketInfo.fromString(txt);
    if (packet != null) {
      System.out.println("PACKET_STR = '" + packet.toString() + "'");
    }
  }
}
