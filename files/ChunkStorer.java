package files;

import java.util.Vector;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;


class ChunkStorer {
  private ConcurrentHashMap<String, Vector<Chunk> > stored_chunks;

  ChunkStorer() {
    this.stored_chunks = new ConcurrentHashMap<String, Vector<Chunk> >();
  }

  private Vector<Chunk> createIfAbsent(String file_id, int size) {
    Vector<Chunk> chunks = this.stored_chunks.get(file_id);
    if (chunks == null) {
      chunks = new Vector<Chunk>(size);
      this.stored_chunks.put(file_id, chunks);
    }
    return chunks;
  }

  boolean addChunk(String file_id, Chunk chunk) {
    Vector<Chunk> chunks = this.createIfAbsent(file_id, chunk.getDesiredRep());

    synchronized (chunks) {
      int index = Collections.binarySearch(chunks, chunk.getChunkN());
      if (index < 0) {
        chunks.add((-(index) - 1), chunk);
      }
      return index < 0;
    }
  }

  boolean removeChunk(String file_id, int chunk_n) {
    Vector<Chunk> chunks = this.stored_chunks.get(file_id);
    if (chunks != null) {
      synchronized (chunks) {
        int index = Collections.binarySearch(chunks, chunk_n);
        if (index >= 0) {
          chunks.remove(index);
        }
        return index >= 0;
      }
    }
    return false;
  }

  boolean removeFile(String file_id) {
    this.stored_chunks.remove(file_id);
    return true;
  }

  boolean addPeer(String file_id, int chunk_n, int peer_id) {
    Chunk chunk = this.getChunk(file_id, chunk_n);

    if (chunk != null) {
      chunk.addPeer(peer_id);
    }

    return chunk != null;
  }

  Chunk getChunk(String file_id, int chunk_n) {
    Vector<Chunk> chunks = this.stored_chunks.get(file_id);
    if (chunks != null) {
      synchronized (chunks) {
        int index = Collections.binarySearch(chunks, chunk_n);
        if (index >= 0) {
          return chunks.get(index);
        }
      }
    }
    return null;
  }

  Vector<Chunk> getFileChunks(String file_id) {
    return this.stored_chunks.get(file_id);
  }

  ConcurrentHashMap<String, Vector<Chunk> > getChunks() {
    return this.stored_chunks;
  }
}
