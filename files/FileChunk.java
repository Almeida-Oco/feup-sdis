package files;

public class FileChunk implements Comparable<FileChunk> {
  private final static int MAX_CHUNK_SIZE = 64000;
  byte[] chunk_data;
  int chunk_n;
  int chunk_size;
  int rep_degree;
  int actual_rep;


  FileChunk(byte[] data, int size, int chunk_n, int degree) {
    this.chunk_data = data;
    this.chunk_size = size;
    this.chunk_n    = chunk_n;
    this.rep_degree = degree;
    this.actual_rep = 0;
  }

  FileChunk(byte[] data, int size, int chunk_n) {
    this.chunk_data = data;
    this.chunk_size = size;
    this.chunk_n    = chunk_n;
    this.rep_degree = 1;
    this.actual_rep = 1;
  }

  public void incActualRep() {
    synchronized (this) {
      this.actual_rep++;
    }
  }

  public byte[] getData() {
    return this.chunk_data;
  }

  public int getSize() {
    return this.chunk_size;
  }

  public int getChunkN() {
    return this.chunk_n;
  }

  public boolean isFinalChunk() {
    return this.chunk_size < MAX_CHUNK_SIZE;
  }

  @Override
  public int compareTo(FileChunk chunk) {
    return Integer.compare(this.chunk_n, ((FileChunk)chunk).chunk_n);
  }
}
