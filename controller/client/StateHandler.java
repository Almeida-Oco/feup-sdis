package controller.client;

import java.rmi.Remote;

class StateHandler implements Remote, Runnable {
  public void run() {
    System.out.println("GOT STATE MSG!");
  }
}
