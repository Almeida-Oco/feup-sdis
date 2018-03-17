package controller.client;

import java.rmi.Remote;

class DeleteHandler implements Remote, Runnable {
  public void run() {
    System.out.println("GOT DELETE MSG!");
  }
}
