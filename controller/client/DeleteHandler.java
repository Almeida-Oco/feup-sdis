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
    FileInfo   file   = File_IO.getFileInfo(this.file_name);
    PacketInfo packet = new PacketInfo("DELETE", file.getID(), -1);

    ScheduledExecutorService schedulor = Executors.newScheduledThreadPool(1);

    for (int i = 0; i < 3; i++) {
      try {
        schedulor.schedule(()->{
          this.mc.sendMsg(packet);
          System.out.println("Sent delete msg");
        }, 2, TimeUnit.SECONDS).get();
      }
      catch (Exception err) {
        System.err.println("Delete::run() -> Interruped scheduler!\n - " + err.getMessage());
      }
    }

    File_IO.eraseLocalFile(this.file_name);
    System.out.println("Erased file '" + this.file_name + "'");
  }
}
