package files;

import java.util.Vector;
import java.util.Collections;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileInfo {
  private final static int MAX_CHUNK_SIZE = 64000;
  String file_name; //Used only for hashing purposes
  String file_id;
  String metadata;  //Used only for hashing purposes
  Vector<FileChunk> chunks;

  FileInfo(File fd, int chunk_number) {
    String abs_path   = fd.getAbsolutePath(),
        last_mod      = Long.toString(fd.lastModified());
    int metadata_size = abs_path.length() + last_mod.length() + 2 * MAX_CHUNK_SIZE;

    //TODO calculate metadata_size differently
    this.metadata  = new String(new byte[metadata_size]);
    this.metadata += abs_path + last_mod;
    this.file_name = abs_path;
    this.chunks    = new Vector<FileChunk>(chunk_number);
    this.file_id   = null;
  }

  FileInfo(String file_id, FileChunk chunk) {
    this.file_id   = file_id;
    this.metadata  = null;
    this.file_name = null;
    this.chunks    = new Vector<FileChunk>();
    this.chunks.add(chunk);
  }

  void addChunk(FileChunk chunk) {
    int index = Collections.binarySearch(this.chunks, chunk);

    if (this.file_id == null) { // Still reading from file
      this.tryHash(chunk);
    }

    this.chunks.add(index, chunk);
  }

  private void tryHash(FileChunk chunk) {
    if (this.chunks.size() == 0) { //Use first chunk as hash
      this.metadata += new String(chunk.getData());
    }

    if (chunk.isFinalChunk()) { //Use last chunk as hash
      this.metadata += new String(chunk.getData());
      this.setFileID();
    }
  }

  private void setFileID() {
    MessageDigest intestine;

    try {
      intestine = MessageDigest.getInstance("SHA-256");
    }
    catch (NoSuchAlgorithmException err) {
      System.err.println("Failed to find encryption algorithm!\n - " + err.getMessage() + "\n - How the frick is this possible?");
      intestine = null;
      System.exit(0);
    }
    this.file_id = new String(intestine.digest(this.metadata.getBytes()));
  }

  public String getName() {
    return this.file_name;
  }

  public String getID() {
    return this.file_id;
  }

  public FileChunk getChunk(int chunk_n) {
    int low = 0, high = this.chunks.size(), mid, number;

    while (low <= high) {
      mid    = (low + high) / 2;
      number = this.chunks.get(mid).getChunkN();

      if (number > chunk_n) {
        high = mid - 1;
      }
      else if (number < chunk_n) {
        low = mid + 1;
      }
      else {
        return this.chunks.get(mid);
      }
    }

    return null;
  }

  public int chunkNumber() {
    return this.chunks.size();
  }

  public Vector<FileChunk> getChunks() {
    return this.chunks;
  }
}
