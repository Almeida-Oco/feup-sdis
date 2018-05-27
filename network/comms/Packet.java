package network.comms;

import java.util.Arrays;
import java.util.Vector;
import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;

public class Packet {
  public static final String RESULT      = "RESULT";
  public static final String NEW_PEER    = "NEW_PEER";
  public static final String CODE        = "CODE";
  public static final String LEAVE       = "LEAVE";
  public static final String ALIVE       = "ALIVE?";
  public static final String HEARTBEAT   = "HEARTBEAT";
  public static final String PREDECESSOR = "PREDECESSOR";
  public static final String FATHER      = "FATHER";
  public static final String GET_PEER    = "GET_PEER";
  public static final String PEER        = "PEER";

  String type;
  Vector<String> params;
  String code;

  private Packet() {
    this.type   = null;
    this.params = new Vector<String>();
    this.code   = null;
  }

  //TODO handle wrong cases
  static Packet fromString(String msg) {
    Packet packet     = new Packet();
    int    header_end = msg.indexOf("\n");
    String header     = msg;

    if (header_end != -1) {
      header = msg.substring(0, header_end);
    }

    packet.code = msg.substring(header_end + 2, msg.length());

    String[] fields = header.split(" ");
    if (fields.length >= 2) {
      packet.type = fields[1];
      for (int i = 2; i < fields.length; i++) {
        packet.params.add(fields[i]);
      }
      return packet;
    }
    else {
      System.err.println("Packet malformed!");
      return null;
    }
  }

  public static Packet newResultPacket(String hash, String result) {
    Packet packet = new Packet();

    packet.type = RESULT;
    packet.params.add(hash);
    packet.code = result;

    return packet;
  }

  public static Packet newNewPeerPacket(String hash, String ip_port, Vector<String> peers) {
    Packet packet = new Packet();

    packet.type = NEW_PEER;
    String[] parameters = { hash, ip_port };
    packet.params.addAll(Arrays.asList(parameters));
    packet.code = "";
    for (String peer : peers) {
      packet.code += peer + " ";
    }
    packet.code = packet.code.substring(0, packet.code.length() - 1);

    return packet;
  }

  public static Packet newCodePacket(String hash, String code) {
    Packet packet = new Packet();

    packet.type = CODE;
    packet.params.add(hash);
    packet.code = code;

    return packet;
  }

  public static Packet newLeavePacket() {
    Packet packet = new Packet();

    packet.type = LEAVE;
    return packet;
  }

  public static Packet newAlivePacket() {
    Packet packet = new Packet();

    packet.type = ALIVE;
    return packet;
  }

  public static Packet newHeartbeatPacket(String hash) {
    Packet packet = new Packet();

    packet.type = HEARTBEAT;
    packet.params.add(hash);
    return packet;
  }

  public static Packet newPredecessorPacket() {
    Packet packet = new Packet();

    packet.type = PREDECESSOR;

    return packet;
  }

  public static Packet newFatherPacket(String hash, String ip_port) {
    Packet packet = new Packet();

    packet.type = FATHER;
    packet.params.add(hash);
    packet.params.add(ip_port);

    return packet;
  }

  public static Packet newGetPeerPacket(String hash) {
    Packet packet = new Packet();

    packet.type = GET_PEER;
    return packet;
  }

  public static Packet newPeerPacket(String hash, String ip_port) {
    Packet packet = new Packet();

    packet.type = PEER;
    packet.params.add(hash);
    packet.params.add(ip_port);

    return packet;
  }

  void setType(String type) {
    this.type = type;
  }

  void addParameter(String param) {
    this.params.add(param);
  }

  void setCode(String code) {
    this.code = code;
  }

  boolean hasType() {
    return this.type != null;
  }

  public String getType() {
    return this.type;
  }

  public Vector<String> getParams() {
    return this.params;
  }

  public long getHash() {
    return Long.parseLong(this.params.get(0));
  }

  public String getIP_Port() {
    return this.params.get(1);
  }

  public String getCode() {
    return this.code;
  }

  @Override
  public String toString() {
    String str = this.type + " ";

    for (String param : this.params) {
      str += param + " ";
    }
    str += "\n";
    if (this.code != null) {
      str += this.code;
    }
    str = str.trim();
    int str_size = str.length();

    int inc = 3;
    if (this.code != null) {
      inc++;
    }

    return "\\" + (str_size + this.numberDigits(str_size) + inc) + "/ " + str;
  }

  private int numberDigits(int number) {
    int cont = 1;

    while ((number = number % 10) > 10) {
      cont++;
      number = number % 10;
    }

    return cont + 1;
  }
}
