package controller.client;

import java.rmi.Remote;

class RestoreHandler implements Remote, Runnable {
  public void run() {
    System.out.println("GOT RESTORE MSG!");
  }
}
