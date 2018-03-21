package network;

import controller.ApplicationInfo;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;


public class PacketInfo {
  private static final String regex = "\\s*(?<msgT>\\w+)\\s+(?<version>\\d\\.\\d)\\s+(?<senderID>\\d+)\\s+(?<fileID>.{64})((\\s+(?<chunkN1>\\d{1,6})\\s+(?<Rdegree>\\d))|\\s+(?<chunkN2>\\d{1,6}))?\\s*\r\n\r\n(?<data>.{0,64000})?";

  private static final Pattern MSG_PAT = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE);

  private static final String CRLF   = "\r\n";
  private static final int HASH_SIZE = 64;

  String msg_type;
  String version;
  String file_id;
  int sender_id;
  int chunk_n;
  int r_degree;
  String data;

  InetAddress addr;
  int port;

  public static void main(String[] args) {
    String  cmp   = "PUTCHUNK 1.0 1 5C7D433B09023F443F3F3F3B3F51190F573F59793F162C393F3F3F3F3F3F4F39 0 1 \r\n\r\n SAD ";
    Matcher match = MSG_PAT.matcher(cmp);

    if (match.matches()) {
      System.out.println("Matches");
    }
    else {
      System.out.println("Does not");
    }
  }

  public PacketInfo(InetAddress addr, int port) {
    this.msg_type = null;
    byte ver = ApplicationInfo.getVersion();

    this.version   = Byte.toString((byte)(ver / 10)) + "." + Byte.toString((byte)(ver % 10));
    this.sender_id = ApplicationInfo.getServID();
    this.file_id   = null;
    this.chunk_n   = -1;
    this.r_degree  = -1;
    this.data      = null;
    this.addr      = addr;
    this.port      = port;
  }

  public static PacketInfo packetWith(String msg_type, String file_id, int chunk_n) {
    PacketInfo packet = new PacketInfo(null, -1);
    byte       ver    = ApplicationInfo.getVersion();

    packet.version   = Byte.toString((byte)(ver / 10)) + "." + Byte.toString((byte)(ver % 10));
    packet.sender_id = ApplicationInfo.getServID();
    packet.msg_type  = msg_type;
    packet.file_id   = file_id;
    packet.chunk_n   = chunk_n;

    return packet;
  }

  public static PacketInfo fromPacket(DatagramPacket packet) {
    String     data       = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.US_ASCII);
    Matcher    match      = MSG_PAT.matcher(data);
    PacketInfo new_packet = new PacketInfo(packet.getAddress(), packet.getPort());

    new_packet.addr = packet.getAddress();
    new_packet.port = packet.getPort();

    System.err.println(" - " + data.substring(0, 81));
    if (!new_packet.fromMatcher(match)) { //TODO should fromMatcher return a PacketInfo?
      return null;
    }

    return new_packet;
  }

  // 5C7D433B09023F443F3F3F3B3F51190F573F59793F162C393F3F3F3F3F3F4F39
  private boolean fromMatcher(Matcher matcher) {
    String chunk_n;

    if (!matcher.matches()) {
      System.err.println("Message does not match pattern!");
      return false;
    }
    try {
      System.out.println("Message matches pattern!");
      this.msg_type  = matcher.group("msgT");
      this.version   = matcher.group("version");
      this.sender_id = Integer.parseInt(matcher.group("senderID"));
      this.file_id   = new String(matcher.group("fileID").getBytes("US-ASCII"), 0, HASH_SIZE, StandardCharsets.US_ASCII);

      if ((chunk_n = matcher.group("chunkN1")) != null) {
        this.chunk_n  = Integer.parseInt(chunk_n);
        this.r_degree = Integer.parseInt(matcher.group("Rdegree"));
      }
      else if ((chunk_n = matcher.group("chunkN2")) != null) {
        this.chunk_n = Integer.parseInt(chunk_n);
      }

      this.data = matcher.group("data");
      return true;
    }
    catch (IllegalArgumentException err) {
      System.err.println("No capturing group in matcher!\n - " + err.getCause() + " -> " + err.getMessage());
      return false;
    }
    catch (IllegalStateException err) {
      System.err.println("Match failed or not done yet!\n - " + err.getMessage());
      return false;
    }
    catch (UnsupportedEncodingException err) {
      System.err.println("Unknown encoding while parsing packet!\n - " + err.getMessage());
      return false;
    }
  }

  public String toString() {
    boolean is_putchunk = this.msg_type.equalsIgnoreCase("PUTCHUNK"),
        is_chunk        = this.msg_type.equalsIgnoreCase("CHUNK");

    if (!this.isReady()) {
      return null;
    }

    return this.headerToString() + CRLF + CRLF + ((is_putchunk || is_chunk) ? this.data : "");
  }

  private String headerToString() {
    boolean is_delete = this.msg_type.equalsIgnoreCase("DELETE"),
        is_putchunk   = this.msg_type.equalsIgnoreCase("PUTCHUNK");

    return this.msg_type + " "
           + this.version + " "
           + this.sender_id + " "
           + this.file_id + " "
           + (is_delete ? "" : (Integer.toString(this.chunk_n) + " "))
           + (is_putchunk ? this.r_degree : "");
  }

  public boolean isReady() {
    boolean is_putchunk = this.msg_type.equalsIgnoreCase("PUTCHUNK"),
        is_stored       = this.msg_type.equalsIgnoreCase("STORED"),
        is_removed      = this.msg_type.equalsIgnoreCase("REMOVED"),
        is_getchunk     = this.msg_type.equalsIgnoreCase("GETCHUNK"),
        is_delete       = this.msg_type.equalsIgnoreCase("DELETE"),
        is_chunk        = this.msg_type.equalsIgnoreCase("CHUNK");

    return this.addr != null&&
           this.port != -1 &&
           (is_putchunk || is_stored || is_removed || is_getchunk || is_delete || is_chunk) &&                             // Check message type
           (this.version != null) &&                                                                                       // Check version
           (this.file_id != null) &&                                                                                       // Check file_id
           (this.sender_id != -1) &&                                                                                       // Check sender id
           ((this.chunk_n != -1 && (is_putchunk || is_stored || is_getchunk || is_removed || is_chunk)) || (is_delete)) && // Check chunk number
           ((this.r_degree != -1 && is_putchunk) || (is_stored || is_getchunk || is_removed || is_delete || is_chunk)) &&  // Check replication degree
           ((this.data != null&& (is_putchunk || is_chunk)) || (is_stored || is_getchunk || is_removed || is_delete));     // Check data
  }

  public void setType(String type) {
    // TODO check if type is correct
    this.msg_type = type;
  }

  public void setVersion(byte ver) {
    this.version = Byte.toString((byte)(ver / 10)) + "." + Byte.toString((byte)(ver % 10));
  }

  public void setFileID(String id) {
    this.file_id = id;
  }

  public void setSenderID(int id) {
    this.sender_id = id;
  }

  public void setChunkN(int n) {
    this.chunk_n = n;
  }

  public void setRDegree(int degree) {
    this.r_degree = degree;
  }

  public void setData(String data) {
    this.data = data;
  }

  public void setData(byte[] data, int size) {
    this.data = new String(data, 0, size, StandardCharsets.US_ASCII);
  }

  public void setAddress(InetAddress addr) {
    this.addr = addr;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getType() {
    return this.msg_type;
  }

  public int getSenderID() {
    return this.sender_id;
  }

  public String getFileID() {
    return this.file_id;
  }

  public byte getVersion() {
    char chr1 = this.version.charAt(0),
        chr2  = this.version.charAt(2);

    return (byte)(Character.getNumericValue(chr1) * 10 + Character.getNumericValue(chr2));
  }

  public int getChunkN() {
    return this.chunk_n;
  }

  public String getData() {
    return this.data;
  }

  public int dataSize() {
    return this.data.length();
  }

  public InetAddress getAddress() {
    return this.addr;
  }

  public int getPort() {
    return this.port;
  }

  public void resetPacket() {
    this.msg_type  = null;
    this.version   = null;
    this.file_id   = null;
    this.sender_id = -1;
    this.chunk_n   = -1;
    this.r_degree  = '\0';
    this.data      = null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PacketInfo) {
      PacketInfo packet = (PacketInfo)obj;
      return this.msg_type.equals(packet.msg_type) &&
             this.file_id.equals(packet.file_id) &&
             this.chunk_n == packet.chunk_n;
    }
    return false;
  }
}
