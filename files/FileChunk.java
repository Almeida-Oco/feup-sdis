package files;

public class FileChunk {
  private final static int MAX_CHUNK_SIZE = 64000;
  byte[] chunk_data;
  int chunk_size;
  int rep_degree;
  int actual_rep;


  FileChunk(byte[] data, int size, int degree) {
    this.chunk_data = data;
    this.chunk_size = size;
    this.rep_degree = degree;
    this.actual_rep = 0;
  }

  FileChunk(byte[] data, int size) {
    this.chunk_data = data;
    this.chunk_size = size;
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

  public boolean isFinalChunk() {
    return this.chunk_size < MAX_CHUNK_SIZE;
  }
}
