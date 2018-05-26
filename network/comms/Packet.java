package network.comms;

import java.util.Vector;
import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;

public class Packet {
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
    int    header_end = msg.indexOf("\r\n");
    String header     = msg.substring(0, header_end);

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

  public static Packet newPacket(String msg_type, Vector<String> params, String code) {
    Packet packet = new Packet();

    packet.type   = msg_type;
    packet.params = params;
    packet.code   = code;

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

  public String getCode() {
    return this.code;
  }

  @Override
  public String toString() {
    String str = "" + this.type + " ";

    for (String param : this.params) {
      str += param + " ";
    }
    str += "\r\n";
    str += this.code;
    int str_size = str.length();
    str = str.trim();

    return "\\" + (str_size + this.numberDigits(str_size) + 5) + "/ " + str;
  }

  private int numberDigits(int number) {
    int cont = 0;

    while ((number = number % 10) > 10) {
      cont++;
    }

    return cont + 1;
  }
}
