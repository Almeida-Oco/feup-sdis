package controller.client;

import java.rmi.Remote;

class ReclaimHandler implements Remote, Runnable {
  public void run() {
    System.out.println("GOT RECLAIM MSG!");
  }
}
