package files;

import controller.Pair;

import java.io.File;
import java.util.Vector;
import java.util.Collections;
import java.security.MessageDigest;
import java.security.DigestException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

public class FileInfo {
  private final static int HASH_SIZE = 32;
  String file_name, metadata; //Used only for hashing purposes

  String file_id;
  Vector<FileChunk> chunks;

  FileInfo(File fd, int chunk_number) {
    String abs_path   = fd.getAbsolutePath(),
        last_mod      = Long.toString(fd.lastModified());
    int metadata_size = abs_path.length() + last_mod.length() + 2 * File_IO.MAX_CHUNK_SIZE;

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
    if (index >= 0) {
      System.out.println("Replicated chunk #" + chunk.getChunkN());
    }
    else {
      this.chunks.add(-(index) - 1, chunk);
    }
  }

  FileChunk popChunk() {
    if (this.chunks.size() > 0) {
      FileChunk first = this.chunks.firstElement();
      this.chunks.remove(0);

      return first;
    }

    return null;
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
    intestine.update(this.metadata.getBytes());
    byte[] hash = new byte[32];

    try {
      int    size   = intestine.digest(hash, 0, HASH_SIZE);
      String middle = new String(hash, 0, HASH_SIZE, StandardCharsets.ISO_8859_1);
      this.file_id = StringToHex.toHex(middle.getBytes(StandardCharsets.ISO_8859_1), HASH_SIZE);
    }
    catch (DigestException err) {
      System.err.println("Failed to digest!\n - " + err.getMessage());
    }
  }

  public String getName() {
    return this.file_name;
  }

  public String getID() {
    return this.file_id;
  }

  public FileChunk getChunk(int chunk_n) {
    int index = FileChunk.binarySearch(this.chunks, chunk_n);

    if (index == -1) {
      System.err.println("Chunk #" + chunk_n + " not stored in file " + this.file_name);
      return null;
    }

    return this.chunks.get(index);
  }

  public int chunkNumber() {
    return this.chunks.size();
  }

  public Vector<FileChunk> getChunks() {
    return this.chunks;
  }

  Vector<Pair<String, FileChunk> > getOverlyReplicated() {
    Vector<Pair<String, FileChunk> > chunks = new Vector<Pair<String, FileChunk> >();
    this.chunks.forEach((chunk)->{
      if (chunk.getActualRep() > chunk.getDesiredRep()) {
        this.chunks.remove(chunk);
        chunks.add(new Pair<String, FileChunk>(this.file_id, chunk));
      }
    });
    return chunks;
  }
}
