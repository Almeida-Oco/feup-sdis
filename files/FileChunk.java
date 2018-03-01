package files;

import java.security.MessageDigest;

public class FileChunk {
  String chunk_id;
  byte chunk_size;
  byte[] chunk_data;
  byte chunk_number;


  FileChunk(String id, byte size, byte[] data, byte number) {
    this.chunk_id     = id;
    this.chunk_size   = size;
    this.chunk_data   = data;
    this.chunk_number = number;

    /*
     * MessageDigest digest = MessageDigest.getInstance("SHA-256");
     *
     * byte[] encodedhash = digest.digest(
     * file_content.getBytes(StandardCharsets.UTF_8));
     *
     * this.file_hash    = String(encodedhash);
     * this.file_content = file_content;
     * this.chunk_number = chunk_number;
     * this.chunk_rep    = chunk_rep;
     */
  }

  /*
   * FileChunk(String file_content) {
   * MessageDigest digest = MessageDigest.getInstance("SHA-256");
   *
   * byte[] encodedhash = digest.digest(
   *  file_content.getBytes(StandardCharsets.UTF_8));
   *
   * this.file_hash    = String(encodedhash);
   * this.file_content = file_content;
   * this.chunk_number = 0;
   * this.chunk_rep    = 0;
   * }
   */
  public String get_id() {
    return this.chunk_id;
  }

  public byte get_size() {
    return this.chunk_size;
  }

  public byte[] get_data() {
    return this.chunk_data;
  }

  public byte get_chunk_number() {
    return this.chunk_number;
  }
}
