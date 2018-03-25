package controller.client;

import network.*;
import files.FileInfo;
import files.File_IO;
import controller.Handler;
import controller.listener.Listener;
import controller.Pair;

import java.rmi.Remote;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

class DeleteHandler extends Handler implements Remote {
  String file_name;
  Listener mc;

  void start(String f_name, Listener mc) {
    this.file_name = f_name;
    this.mc        = mc;
    this.run();
  }

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
    FileInfo      file   = File_IO.getFileInfo(this.file_name);
    String        id     = file.getID();
    AtomicInteger count  = new AtomicInteger(0);
    PacketInfo    packet = new PacketInfo(this.mc.getAddr(), this.mc.getPort());

    packet.setType("DELETE");
    packet.setFileID(id);

    ScheduledExecutorService schedulor = Executors.newScheduledThreadPool(1);
    ScheduledFuture          future    = schedulor.scheduleAtFixedRate(()->{
      this.mc.sendMsg(packet);
      count.incrementAndGet();
      File_IO.eraseFile(this.file_name);
    }, 0, 2, TimeUnit.SECONDS);

    while (count.get() < 5) {
    }

    future.cancel(false);
  }
}
