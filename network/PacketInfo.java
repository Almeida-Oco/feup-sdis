package network;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PacketInfo {
  private static final Pattern msg_pattern =
    Pattern.compile(" *(?<msgT>\\w+) +(?<version>\\d.\\d) +(?<senderID>\\d+) +(?<fileID>.{64}) +(?<chunkN>\\d{1,6}) +(?<Rdegree>\\d) *\r\n\r\n(?<data>.{0,64000})\r\n");
  final String CRLF = "\r\n";

  String msg_type;
  String version;
  String file_id;
  int sender_id;
  int chunk_n;
  char r_degree;

  String data;

  PacketInfo() {
    this.msg_type = "";
    this.version = "";
    this.file_id = "";
    this.sender_id = -1;
    this.chunk_n = -1;
    this.r_degree = '\0';
  }


  private String headerToString() {
    return this.msg_type + " " +
    this.version + " " +
    this.file_id + " " +
    Integer.toString(this.sender_id) + " " +
    Integer.toString(this.chunk_n) + " " +
    r_degree + " " + this.CRLF + this.CRLF;
  }

  public static PacketInfo fromString(String str) {
    Matcher match = PacketInfo.msg_pattern.matcher(str);
    PacketInfo packet = new PacketInfo();

    if (match.matches() && !packet.fromMatcher(match)) { //TODO should fromMatcher return a PacketInfo?
      return null;
    }
    return packet;
  }

  private boolean fromMatcher (Matcher matcher)  {
    try {
      this.msg_type   = matcher.group("msgT");
      this.version    = matcher.group("version");
      this.sender_id  = Integer.parseInt(matcher.group("senderID"));
      this.file_id    = matcher.group("fileID");
      this.chunk_n    = Integer.parseInt(matcher.group("chunkN"));
      this.r_degree   = matcher.group("Rdegree").charAt(0);
      this.data       = matcher.group("data");
      return true;
    }
    catch (IllegalArgumentException err) {
      System.err.println("No capturing group in matcher!\n " + err.getCause() + " -> " + err.getMessage());
      return false;
    }
    catch (IllegalStateException err) {
      System.err.println("Match failed or not done yet!\n " + err.getMessage());
      return false;
    }
  }

  public String toString() {
    return this.headerToString() + this.data + this.CRLF;
  }


  public void set_msg_type(String type) {
    // TODO check if type is correct
    this.msg_type = type;
  }

  public void set_version(byte major, byte minor) {
    this.version = Byte.toString(major) + "." + Byte.toString(minor);
  }

  public void set_file_id(String id) {
    this.file_id = id;
  }

  public void set_sender_id(int id) {
    this.sender_id = id;
  }

  public void set_chunk_n(int n) {
    this.chunk_n = n;
  }

  public void set_r_degree(char degree) {
    this.r_degree = degree;
  }

  public void set_data(String data) {
    this.data = data;
  }
}
