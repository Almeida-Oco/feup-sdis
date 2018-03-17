package controller.client;

import java.rmi.Remote;

class BackupHandler implements Remote, Runnable {
  public void run() {
    System.out.println("GOT BACKUP MSG!");
  }
}
