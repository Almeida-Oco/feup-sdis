package network;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PacketInfo {
  private static final Pattern MSG_PAT = Pattern.compile(" *(?<msgT>\\w+) +(?<version>\\d.\\d) +(?<senderID>\\d+) +(?<fileID>.{64})( +(?<chunkN1>\\d{1,6}) +(?<Rdegree>\\d)| +(?<chunkN2>\\d{1,6}))? *\r\n\r\n(?<data>.{0,64000})?", Pattern.CASE_INSENSITIVE);

  // private static final Pattern PUTCHUNK_PAT =
  //   Pattern.compile(" *PUTCHUNK +(?<version>\\d.\\d) +(?<senderID>\\d+) +(?<fileID>.{64}) +(?<chunkN>\\d{1,6}) +(?<Rdegree>\\d) *\r\n\r\n(?<data>.{0,64000})", Pattern.CASE_INSENSITIVE);
  // private static final Pattern STORED_PAT =
  //   Pattern.compile(" *STORED +(?<version>\\d.\\d) +(?<senderID>\\d+) +(?<fileID>.{64}) +(?<chunkN>\\d{1,6}) *\r\n\r\n", Pattern.CASE_INSENSITIVE);
  // private static final Pattern GETCHUNK_PAT =
  //   Pattern.compile(" *GETCHUNK +(?<version>\\d.\\d) +(?<senderID>\\d+) +(?<fileID>.{64}) +(?<chunkN>\\d{1,6}) *\r\n\r\n", Pattern.CASE_INSENSITIVE);
  // private static final Pattern CHUNK_PAT =
  //   Pattern.compile(" *CHUNK +(?<version>\\d.\\d) +(?<senderID>\\d+) +(?<fileID>.{64}) +(?<chunkN>\\d{1,6}) *\r\n\r\n(?<data>.{0,64000})", Pattern.CASE_INSENSITIVE);
  // private static final Pattern DELETE_PAT =
  //   Pattern.compile(" *DELETE +(?<version>\\d.\\d) +(?<senderID>\\d+) +(?<fileID>.{64}) *\r\n\r\n", Pattern.CASE_INSENSITIVE);
  // private static final Pattern REMOVED_PAT =
  //   Pattern.compile(" *REMOVED +(?<version>\\d.\\d) +(?<senderID>\\d+) +(?<fileID>.{64}) +(?<chunkN>\\d{1,6}) *\r\n\r\n", Pattern.CASE_INSENSITIVE);
  private static final String CRLF = "\r\n";

  String msg_type;
  String version;
  String file_id;
  int sender_id;
  int chunk_n;
  byte r_degree;
  String data;


  public static void main(String[] args) {
  }

  PacketInfo() {
    this.msg_type = null;

    this.version   = null;
    this.file_id   = null;
    this.sender_id = -1;
    this.chunk_n   = -1;
    this.r_degree  = -1;
    this.data      = null;
  }

  public static PacketInfo fromString(CharSequence str) {
    Matcher    match  = PacketInfo.MSG_PAT.matcher(str);
    PacketInfo packet = new PacketInfo();

    if (match.matches() && !packet.fromMatcher(match)) { //TODO should fromMatcher return a PacketInfo?
      return null;
    }
    return packet;
  }

  private boolean fromMatcher(Matcher matcher) {
    String chunk_n;

    try {
      this.msg_type  = matcher.group("msgT");
      this.version   = matcher.group("version");
      this.sender_id = Integer.parseInt(matcher.group("senderID"));
      this.file_id   = matcher.group("fileID");

      if ((chunk_n = matcher.group("chunkN1")) != null) {
        this.chunk_n  = Integer.parseInt(chunk_n);
        this.r_degree = Byte.parseByte(matcher.group("Rdegree"));
      }
      else if ((chunk_n = matcher.group("chunkN2")) != null) {
        this.chunk_n = Integer.parseInt(chunk_n);
      }

      this.data = matcher.group("data");
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
    boolean is_putchunk = this.msg_type.equalsIgnoreCase("PUTCHUNK"),
            is_chunk    = this.msg_type.equalsIgnoreCase("CHUNK");

    if (!this.isReady()) {
      return null;
    }

    return this.headerToString() + ((is_putchunk || is_chunk) ? this.data : "");
  }

  private String headerToString() {
    boolean is_delete   = this.msg_type.equalsIgnoreCase("DELETE"),
            is_putchunk = this.msg_type.equalsIgnoreCase("PUTCHUNK");


    return this.msg_type + " "
           + this.version + " "
           + this.file_id + " "
           + Integer.toString(this.sender_id) + " "
           + (is_delete ? "" : Integer.toString(this.chunk_n) + " ")
           + (is_putchunk ? this.r_degree + " " : "")
           + CRLF + CRLF;
  }

  public boolean isReady() {
    boolean is_putchunk = this.msg_type.equalsIgnoreCase("PUTCHUNK"),
            is_stored   = this.msg_type.equalsIgnoreCase("STORED"),
            is_removed  = this.msg_type.equalsIgnoreCase("REMOVED"),
            is_getchunk = this.msg_type.equalsIgnoreCase("GETCHUNK"),
            is_delete   = this.msg_type.equalsIgnoreCase("DELETE"),
            is_chunk    = this.msg_type.equalsIgnoreCase("CHUNK");

    return
      (is_putchunk || is_stored || is_removed || is_getchunk || is_delete || is_chunk) &&                                                                          // Check message type
      (this.version != null) &&                                                                                                                                    // Check version
      (this.file_id != null) &&                                                                                                                                    // Check file_id
      (this.sender_id != -1) &&                                                                                                                                    // Check sender id
      ((this.chunk_n != -1 && (is_putchunk || is_stored || is_getchunk || is_removed || is_chunk)) || (this.chunk_n == -1 && (is_delete))) &&                      // Check chunk number
      ((this.r_degree != -1 && is_putchunk) || (this.r_degree == -1 && (is_stored || is_getchunk || is_removed || is_delete || is_chunk))) &&                      // Check replication degree
      ((this.data != null&& (is_putchunk || is_chunk)) || ((this.data == null || this.data.equals("")) && (is_stored || is_getchunk || is_removed || is_delete))); // Check data
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

  public void set_r_degree(byte degree) {
    this.r_degree = degree;
  }

  public void set_data(String data) {
    this.data = data;
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
}
