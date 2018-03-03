package files;

import java.security.MessageDigest;

public class FileChunk {
  String chunk_id;
  byte[] chunk_data;
  byte chunk_number;


  FileChunk(byte[] data, byte number) {
    MessageDigest intestine = MessageDigest.getInstance("SHA-256");

    this.chunk_id     = new String(intestine.digest(data));
    this.chunk_data   = data;
    this.chunk_number = number;
  }

  public String get_id() {
    return this.chunk_id;
  }

  public byte[] get_data() {
    return this.chunk_data;
  }

  public byte get_chunk_number() {
    return this.chunk_number;
  }
}
