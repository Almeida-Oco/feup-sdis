package controller.client;

import cli.User_IO;
import cli.Files;
import files.*;
import network.PacketInfo;
import controller.Handler;
import controller.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Vector;
import java.rmi.Remote;

class StateHandler extends Handler implements Remote {
  @Override
  public void signal(PacketInfo packet) {
  }

  @Override
  public Pair<String, Handler> register() {
    return null;
  }

  @Override
  public String signalType() {
    return null;
  }

  @Override
  public void run() {
    ConcurrentHashMap<String, FileInfo> file_table = File_IO.getTable();
    Files file_info = new Files();

    file_table.forEach(1, (file_name, info)->{
      file_info.addNewFile(file_name);
      Vector<FileChunk> chunks = info.getChunks();
      chunks.forEach((chunk)->{
        file_info.addChunk(file_name, chunk.getChunkN(), chunk.getSize());
      });
    });

    User_IO.printState(file_info);
    System.out.println("GOT STATE MSG!");
  }
}
