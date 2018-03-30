package controller.client;

import network.*;
import files.File_IO;
import files.FileInfo;
import controller.Pair;
import controller.Handler;
import controller.ChannelListener;

import java.rmi.Remote;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Handler for the Delete instruction from the client
 * @author Gonçalo Moreno
 * @author João Almeida
 */
class DeleteHandler extends Handler implements Remote {
  /** The path to the file to be deleted */
  String file_name;

  /** The instance of MC {@link ChannelListener} */
  ChannelListener mc;

  /**
   * Initializes the {@link DeleteHandler} with the given arguments and runs it
   * @param f_name Path to file to be deleted
   * @param mc     Instance of MC {@link ChannelListener}
   */
  void start(String f_name, ChannelListener mc) {
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
    FileInfo file = File_IO.getFileInfo(this.file_name);

    if (file == null) {
      return;
    }
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
