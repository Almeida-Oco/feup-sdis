package controller.client;

import network.*;
import files.FileHandler;
import files.FileInfo;
import controller.Handler;

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

  /** The instance of MC {@link Net_IO} */
  Net_IO mc;

  /**
   * Initializes the {@link DeleteHandler} with the given arguments and runs it
   * @param f_name Path to file to be deleted
   * @param mc     Instance of MC {@link Net_IO}
   */
  void start(String f_name, Net_IO mc) {
    this.file_name = f_name;
    this.mc        = mc;
    this.run();
  }

  @Override
  public void signal(PacketInfo packet) {
  }

  @Override
  public void run() {
    FileInfo file = FileHandler.getBackedFileByName(this.file_name);

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

    FileHandler.eraseBackedFile(this.file_name);
    System.out.println("Erased file '" + this.file_name + "'");
  }
}
