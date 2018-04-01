package network;

import controller.ApplicationInfo;

import java.util.List;
import java.util.Scanner;
import java.net.InetAddress;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.DatagramPacket;
import java.util.InputMismatchException;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

/**
 * The middle man between the messages exchanged by the network and the program
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class PacketInfo {
  private static final String regex = "\\s*(?<msgT>\\w+)\\s+(?<version>\\d\\.\\d)\\s+(?<senderID>\\d+)\\s+(?<fileID>.{64})((\\s+(?<chunkN1>\\d{1,6})\\s+(?<Rdegree>\\d))|\\s+(?<chunkN2>\\d{1,6}))?\\s*\r\n(?<misc>.*?\r\n)*\r\n(?<data>.{0,64000})?";

  private static final Pattern MSG_PAT = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE);

  private static final String CRLF   = "\r\n";
  private static final int HASH_SIZE = 64;

  /** Type of message */
  String msg_type;
  /** Version of message */
  String version;
  /** ID of file */
  String file_id;
  /** ID of the sender of the message */
  int sender_id;
  /** Number of the chunk */
  int chunk_n;
  /** Desired replication degree */
  int r_degree;
  /** Data of message */
  String data;

  /** The replicators of the chunk */
  int[] replicators;

  /**
   * Initializes a new {@link PacketInfo}
   */
  private PacketInfo() {
  }

  /**
   * Initializes a new {@link PacketInfo}
   * @param msg_type Type of message
   * @param file_id  ID of file
   * @param chunk_n  Number of chunk
   */
  public PacketInfo(String msg_type, String file_id, int chunk_n) {
    byte ver = ApplicationInfo.getVersion();

    this.version   = Byte.toString((byte)(ver / 10)) + "." + Byte.toString((byte)(ver % 10));
    this.sender_id = ApplicationInfo.getServID();
    this.msg_type  = msg_type;
    this.file_id   = file_id;
    this.chunk_n   = chunk_n;
  }

  /**
   * Initializes a new {@link PacketInfo} from a {@link DatagramPacket}
   * @param  packet Packet to use for initialization
   * @return        The newly created {@link PacketInfo}
   */
  public static PacketInfo fromPacket(DatagramPacket packet) {
    String     data       = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.ISO_8859_1);
    Matcher    match      = MSG_PAT.matcher(data);
    PacketInfo new_packet = new PacketInfo();

    if (!new_packet.fromMatcher(match)) { //TODO should fromMatcher return a PacketInfo?
      return null;
    }

    return new_packet;
  }

  /**
   * Initializes the given members from the Regex {@link Matcher}
   * @param  matcher Regular expression matcher
   * @return         Whether all fields were initialized successfully or not
   */
  private boolean fromMatcher(Matcher matcher) {
    String chunk_n;
    String misc;

    if (!matcher.matches()) {
      System.err.println("Message does not match pattern!");
      return false;
    }
    try {
      this.msg_type  = matcher.group("msgT");
      this.version   = matcher.group("version");
      this.sender_id = Integer.parseInt(matcher.group("senderID"));
      this.file_id   = matcher.group("fileID");
      this.data      = matcher.group("data");

      if ((chunk_n = matcher.group("chunkN1")) != null) {
        this.chunk_n  = Integer.parseInt(chunk_n);
        this.r_degree = Integer.parseInt(matcher.group("Rdegree"));
        if (this.msg_type.equalsIgnoreCase("CHUNKCHKS") && this.r_degree > 0) {
          return this.parseReplicators(matcher.group("misc"), this.r_degree);
        }
      }
      else if ((chunk_n = matcher.group("chunkN2")) != null) {
        this.chunk_n = Integer.parseInt(chunk_n);
      }


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
  }

  private boolean parseReplicators(String reps, int rep_number) {
    this.replicators = new int[rep_number];
    Scanner scan = new Scanner(reps);
    int     i = 0, rep;
    try {
      while (scan.hasNextInt()) {
        rep = scan.nextInt();
        this.replicators[i++] = rep;
      }
    }
    catch (InputMismatchException err) {
      System.err.println("Not a CHUNKCHKS packet!");
      this.replicators = null;
      return false;
    }

    return true;
  }

  /**
   * Converts this packet into a string ready to be sent using {@link DatagramPacket}
   * @return {@link String} representation of {@link PacketInfo}
   */
  public String toString() {
    boolean is_putchunk = this.msg_type.equalsIgnoreCase("PUTCHUNK"),
        is_chunk        = this.msg_type.equalsIgnoreCase("CHUNK"),
        is_chunkchks    = this.msg_type.equalsIgnoreCase("CHUNKCHKS");

    if (!this.isReady()) {
      return null;
    }

    return this.headerToString() + CRLF +
           (is_chunkchks ? (this.repsToString() + CRLF) : "") + CRLF +
           ((is_putchunk || is_chunk) ? this.data : "");
  }

  private String repsToString() {
    String reps = new String();

    for (int i = 0; i < this.replicators.length; i++) {
      reps += Integer.toString(this.replicators[i]) + " ";
    }
    return reps;
  }

  /**
   * Converts the header part of the packet to a string
   * @return {@link String} representation of the header of {@link PacketInfo}
   */
  private String headerToString() {
    boolean is_delete = this.msg_type.equalsIgnoreCase("DELETE"),
        is_putchunk   = this.msg_type.equalsIgnoreCase("PUTCHUNK"),
        is_chunkchks  = this.msg_type.equalsIgnoreCase("CHUNKCHKS");

    return this.msg_type + " "
           + this.version + " "
           + this.sender_id + " "
           + this.file_id + " "
           + (is_delete ? "" : (Integer.toString(this.chunk_n) + " "))
           + ((is_putchunk || is_chunkchks) ? this.r_degree : "");
  }

  /**
   * Checks if packet is ready to be sent
   * @return Whether packet is ready to be sent or not
   */
  public boolean isReady() {
    boolean is_putchunk = this.msg_type.equalsIgnoreCase("PUTCHUNK"),
        is_stored       = this.msg_type.equalsIgnoreCase("STORED"),
        is_removed      = this.msg_type.equalsIgnoreCase("REMOVED"),
        is_getchunk     = this.msg_type.equalsIgnoreCase("GETCHUNK"),
        is_delete       = this.msg_type.equalsIgnoreCase("DELETE"),
        is_chunk        = this.msg_type.equalsIgnoreCase("CHUNK"),
        is_chkchunk     = this.msg_type.equalsIgnoreCase("CHKCHUNK"),
        is_chunkchks    = this.msg_type.equalsIgnoreCase("CHUNKCHKS");

    return
      (is_putchunk || is_stored || is_removed || is_getchunk || is_delete || is_chunk || is_chkchunk || is_chunkchks) &&                             // Check message type
      (this.version != null) &&                                                                                                                      // Check version
      (this.file_id != null) &&                                                                                                                      // Check file_id
      (this.sender_id != -1) &&                                                                                                                      // Check sender id
      ((this.chunk_n != -1 && (is_putchunk || is_stored || is_getchunk || is_removed || is_chunk || is_chkchunk || is_chunkchks)) || (is_delete)) && // Check chunk number
      ((this.r_degree != -1 && is_putchunk || is_chunkchks) || (is_stored || is_getchunk || is_removed || is_delete || is_chunk || is_chkchunk)) &&  // Check replication degree
      ((this.data != null&& (is_putchunk || is_chunk)) || (is_stored || is_getchunk || is_removed || is_delete || is_chkchunk || is_chunkchks)) &&   // Check data
      ((this.replicators != null&& is_chunkchks) || !is_chunkchks);
  }

  /**
   * Sets the type of message
   * @param type Type of message
   */
  public void setType(String type) {
    // TODO check if type is correct
    this.msg_type = type;
  }

  /**
   * Sets the version of the message
   * @param ver Version of message
   */
  public void setVersion(byte ver) {
    this.version = Byte.toString((byte)(ver / 10)) + "." + Byte.toString((byte)(ver % 10));
  }

  /**
   * Sets the file ID of the message
   * @param id ID of file
   */
  public void setFileID(String id) {
    this.file_id = id;
  }

  /**
   * Sets the sender ID of the message
   * @param id ID of sender
   */
  public void setSenderID(int id) {
    this.sender_id = id;
  }

  /**
   * Sets the chunk number of the message
   * @param n Chunk number
   */
  public void setChunkN(int n) {
    this.chunk_n = n;
  }

  /**
   * Sets the actual replication degree of the message
   * @param degree Replication degree
   */
  public void setRDegree(int degree) {
    this.r_degree = degree;
  }

  public void setReplicators(List<Integer> reps) {
    this.replicators = new int[reps.size()];
    int i = 0;
    for (Integer rep : reps) {
      this.replicators[i++] = rep;
    }
  }

  /**
   * Sets the data of the message
   * @param data {@link String} representation of the data
   */
  public void setData(String data) {
    this.data = data;
  }

  /**
   * Sets the data of the message
   * @param data The data to be used
   * @param size Size of data
   */
  public void setData(byte[] data, int size) {
    this.data = new String(data, 0, size, StandardCharsets.ISO_8859_1);
  }

  /**
   * Gets the type of the message
   * @return {@link PacketInfo#msg_type}
   */
  public String getType() {
    return this.msg_type;
  }

  /**
   * Gets the sender ID of the message
   * @return {@link PacketInfo#sender_id}
   */
  public int getSenderID() {
    return this.sender_id;
  }

  /**
   * Gets the file ID of the message
   * @return {@link PacketInfo#file_id}
   */
  public String getFileID() {
    return this.file_id;
  }

  /**
   * Gets the version of the message
   * @return Version of message, major digit = major version, minor digit = minor version
   */
  public byte getVersion() {
    char chr1 = this.version.charAt(0),
        chr2  = this.version.charAt(2);

    return (byte)(Character.getNumericValue(chr1) * 10 + Character.getNumericValue(chr2));
  }

  public int[] getReplicators() {
    return this.replicators;
  }

  /**
   * Gets the replication degree of the message
   * @return {@link PacketInfo#r_degree}
   */
  public int getRDegree() {
    return this.r_degree;
  }

  /**
   * Gets the chunk number of the message
   * @return {@link PacketInfo#chunk_n}
   */
  public int getChunkN() {
    return this.chunk_n;
  }

  /**
   * Gets the dat of the message
   * @return {@link PacketInfo#data}
   */
  public String getData() {
    return this.data;
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
