package controller.handler;

import network.*;
import files.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

class GetchunkHandler extends Handler {
  byte version;
  String file_id;
  int chunk_n;
  ThreadPoolExecutor task_queue;
  boolean stop_listening;
  PacketInfo curr_packet;
  Thread listener;


  GetchunkHandler(PacketInfo packet, Net_IO mc, Net_IO mdr, Net_IO mdb, ThreadPoolExecutor queue) {
    super(mc, mdr, mdb);
    this.version        = packet.getVersion();
    this.file_id        = packet.getFileID();
    this.chunk_n        = packet.getChunkN();
    this.sender_addr    = packet.getAddress();
    this.sender_port    = packet.getPort();
    this.curr_packet    = packet;
    this.task_queue     = queue;
    this.stop_listening = false;

    this.listener = new Thread() {
      public void run() {
        boolean    stop;
        PacketInfo recv_packet;

        synchronized (this) {
          stop = this.stop_listening;
        }
        while (!stop) {
          if ((recv_packet = this.mdb.recvMsg()) != null) {
            if (this.isSamePacket(this.curr_packet, recv_packet)) {
              synchronized (this) {
                this.stop_listening = true;
              }
            }
            this.task_queue.execute(Handler.newHandler(recv_packet, this.mc, this.mdr, this.mdb, this.task_queue));
          }
        }
      }
    };
  }

  public void run() {
    FileChunk chunk = File_IO.getChunk(this.file_id, this.chunk_n);

    if (chunk != null) {
      PacketInfo packet = new PacketInfo(this.sender_addr, this.sender_port);

      this.listener.start();
      packet.setType("CHUNK");
      packet.setVersion(this.version);
      packet.setFileID(this.file_id);
      packet.setChunkN(this.chunk_n);
      packet.setData(new String(chunk.getData(), StandardCharsets.US_ASCII));
      Random rand = new Random();

      Thread.sleep(rand.nextInt(401)); //TODO use ScheduledExecutorService?
      if (!this.stop_listening) {
        mdr.sendMsg(packet);
        synchronized (this) {
          this.stop_listening = true;
        }
      }
    }
  }

  private boolean isSamePacket(PacketInfo curr_packet, PacketInfo new_packet) {
    return curr_packet.getFileID().equals(new_packet.getFileID()) &&
           curr_packet.getChunkN() == new_packet.getChunkN();
  }
}
